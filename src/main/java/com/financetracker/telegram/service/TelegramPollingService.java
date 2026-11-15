package com.financetracker.telegram.service;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.financetracker.core.model.Category;
import com.financetracker.core.model.Transaction;
import com.financetracker.core.model.User;
import com.financetracker.core.repository.CategoryRepository;
import com.financetracker.core.repository.TransactionRepository;
import com.financetracker.core.repository.UserRepository;
import com.financetracker.exception.*;
import com.financetracker.telegram.TelegramClient;
import com.financetracker.telegram.dto.*;
import com.financetracker.telegram.model.BotOffset;
import com.financetracker.telegram.model.TelegramLink;
import com.financetracker.telegram.model.TelegramMessageTransaction;
import com.financetracker.telegram.repository.BotOffsetRepository;
import com.financetracker.telegram.repository.TelegramLinkRepository;
import com.financetracker.telegram.repository.TelegramMessageTransactionRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class TelegramPollingService {
    private final TelegramClient telegramClient;
    private final UserRepository userRepository;
    private final BotOffsetRepository botOffsetRepository;
    private final TelegramLinkRepository telegramLinkRepository;
    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final TelegramMessageTransactionRepository telegramMessageTransactionRepository;

    private static final SecureRandom RANDOM = new SecureRandom();

    private static final String START_CMD = "/start";
    private static final String START_MSG = "Paste this Code on the Web App to validate the chat with the Bot";
    private static final String AUTH_SUCCESS_MSG = "Account Authentication Successful. You will now receive updates about your daily transactions.\nPLEASE READ THE INSTRUCTIONS IN BOT DESCRIPTION FOR BETTER EXPERIENCE";
    private static final String UPDATE_NOT_ALLOWED_MSG = "The Transaction is already categorized. Please update the category from the application";

    private static final Pattern CUSTOM_CATEGORY_PATTERN = Pattern.compile("^\\(([a-zA-Z0-9]+)\\)$");

    public TelegramPollingService(TelegramClient telegramClient,
                                  UserRepository userRepository,
                                  BotOffsetRepository botOffsetRepository,
                                  TelegramLinkRepository telegramLinkRepository,
                                  TransactionRepository transactionRepository,
                                  CategoryRepository categoryRepository,
                                  TelegramMessageTransactionRepository telegramMessageTransactionRepository) {
        this.telegramClient = telegramClient;
        this.userRepository = userRepository;
        this.botOffsetRepository = botOffsetRepository;
        this.telegramLinkRepository = telegramLinkRepository;
        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.telegramMessageTransactionRepository = telegramMessageTransactionRepository;
    }

    public boolean handleTelegramConnection(UUID userId, TelegramLinkRequest linkRequest) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotRegisteredException("User not found"));
        TelegramLink link = telegramLinkRepository.findByCode(linkRequest.code())
                .orElseThrow(CodeInvalidException::new);

        if(user.getTelegramChatId() != null) {
            throw new UserAlreadyLinkedException();
        }

        if(link.getExpiresAt().isAfter(Instant.now())) {
            user.setTelegramChatId(link.getChatId());
            userRepository.save(user);
            telegramLinkRepository.delete(link);
            telegramClient.sendMessage(link.getChatId(), AUTH_SUCCESS_MSG, null);
            return true;
        } else {
            throw new CodeExpiredException();
        }
    }

    @Scheduled(fixedDelay = 5000L)
    private void pollTelegramMessage() {
        BotOffset offsetRecord = botOffsetRepository.findFirstByOrderByCreatedAtDesc().orElse(new BotOffset(1L));
        Long offset = offsetRecord.getLastOffset();

        TelegramUpdateResponse updates = telegramClient.getUpdate(offset);

        if(!updates.ok()) {
            // ADD LOGGING
            return;
        }
        if(updates.result().isEmpty()) {
            // ADD LOGGING
            return;
        }

        Long newLastOffset = handleBotUpdate(updates);
        offsetRecord.setLastOffset(newLastOffset);
        botOffsetRepository.save(offsetRecord);
    }

//    @Scheduled(fixedDelay =  5 * 60 * 1000L)
    public void pollRabbitMqMessage(UUID userId, UUID transactionId) {
        User user = userRepository.findById(userId).orElseThrow(UserNotRegisteredException::new);
        Long chatId = user.getTelegramChatId();
        if(chatId == null) {
            throw new UserNotLinkedException();
        }
        List<Category> categories = categoryRepository.findByUserIdOrIsDefaultTrue(userId);
        Transaction transaction = transactionRepository.findById(transactionId).orElseThrow(
                () -> new TransactionNotFoundException(transactionId)
        );

        TelegramMessageRequest messageRequest = generateNewCategorizationMessage(chatId, transaction, categories);
        TelegramMessageResponse messageSendResponse = telegramClient.sendMessage(messageRequest);
        // Add logging for response
        if(!messageSendResponse.ok()) {
            // ADD LOGGING
            throw new TelegramSendMessageException();
        }

        Long messageId = messageSendResponse.result().messageId();

        transaction.setCategorizationStatus(Transaction.CategorizationStatus.NOTIFIED);
        transactionRepository.save(transaction);

        TelegramMessageTransaction messageTransactionRecord = new TelegramMessageTransaction(chatId, messageId, transactionId);
        telegramMessageTransactionRepository.save(messageTransactionRecord);
    }

    /// Can we use threads to start processing of multiple results simultaneously
    /// Loop over each update in the result
    /// For each message, check if the message is a command, by checking if it starts with '/'
    /// If message is '/start', then check if a user exists for this chat_id
    /// If it doesn't, then send a message with a 6-digit code, else just send a message saying bot is already started
    /// If the message came with reply to an earlier question, create a nwe category ad update the category
    /// of the corresponding transaction as well.
    /// If the update is a callback query then just update the category of the corresponding transaction
    /// @param updates: Latest updates from the last updated offset
    private Long handleBotUpdate(TelegramUpdateResponse updates) {
        long maxUpdateOffset = 0L;
        for(TelegramUpdateResponse.Result result : updates.result()) {
            maxUpdateOffset = Math.max(maxUpdateOffset, result.updateId());
            if(result.callbackQuery() != null) {
                handleCallbackQueryResponse(result.callbackQuery());
            } else if(result.message() != null && result.message().text() != null) {
                if(result.message().text().equals(START_CMD)) {
                    //A command: To be processed separately
                    handleNewChatInit(result.message());
                } else {
                    handleMessageResponse(result.message());
                }
            }
            // TODO: Add logging for malformed or non-text input
        }
        return maxUpdateOffset + 1;
    }

    private void handleCallbackQueryResponse(TelegramUpdateResponse.CallbackQuery callbackQuery) {
        String[] callbackData = callbackQuery.data().split(":");
        String encodedTransactionId = callbackData[1];
        String encodedCategoryId = callbackData[2];
        UUID transactionId = decompactUuid(encodedTransactionId);
        UUID categoryId = decompactUuid(encodedCategoryId);

        boolean isCategoryIdValid = categoryRepository.existsById(categoryId);
        if(!isCategoryIdValid) {
            // TODO: ADD LOGGING
            return;
        }

        Optional<Transaction> selectedPossibleTransaction = transactionRepository.findById(transactionId);
        if(selectedPossibleTransaction.isEmpty()) {
            // TODO: ADD LOGGING
            return;
        }

        Transaction selectedTransaction = selectedPossibleTransaction.get();
        if(selectedTransaction.getCategorizationStatus().equals(Transaction.CategorizationStatus.CATEGORIZED)) {
            TelegramMessageResponse res = telegramClient.sendMessage(callbackQuery.message().chat().id(), UPDATE_NOT_ALLOWED_MSG, null);
            if(!res.ok()) {
                // TODO: ADD LOGGING
            }
            return;
        }

        selectedTransaction.setCategorizationStatus(Transaction.CategorizationStatus.CATEGORIZED);
        selectedTransaction.setCategoryId(categoryId);
        transactionRepository.save(selectedTransaction);
    }

    /// Message must be reply to the original message
    /// New category should be wrapped in '()', must be unique and alphanumeric
    private void handleMessageResponse(TelegramUpdateResponse.MessageDetails messageDetails) {
        // Validate the message replies to a particular message
        if(messageDetails.replyToMessage() == null) {
            return;
        }
        // Validate the replied to message is actually a categorization bot message
        TelegramUpdateResponse.MessageDetails replyToMessage = messageDetails.replyToMessage();
        Long chatId = replyToMessage.chat().id();
        Long messageId = replyToMessage.messageId();
        Optional<TelegramMessageTransaction> messageTransactionRecord =
                telegramMessageTransactionRepository.findByChatIdAndMessageId(
                        chatId, messageId
                );
        if (messageTransactionRecord.isEmpty()) {
            // Replied-To Message not a transaction categorization message
            // ADD LOGGING;
            return;
        }
        Optional<User> userDetails = userRepository.findByTelegramChatId(chatId);
        if(userDetails.isEmpty()) {
            // User not found for this telegram chat
            return;
        }
        UUID userId = userDetails.get().getId();
        // Validate transaction actually exists
        UUID transactionId = messageTransactionRecord.get().getTransactionId();
        Optional<Transaction> transactionDetails = transactionRepository.findById(transactionId);
        if(transactionDetails.isEmpty()) {
            // Transaction for the given message-transaction record not found. Something tampered with the DB
            return;
        }
        // Validate new category passes all the necessary checks
        if(!isNewCategoryMessageValid(messageDetails.text())) {
            // Category Does not pass the requirement
            return;
        }
        // Validate the category is unique
        String suggestedCategoryName = messageDetails.text().substring(1, messageDetails.text().length() - 1);
        if(categoryRepository.existsByNameAndUserId(suggestedCategoryName, userId)) {
            // Same category already exists
            return;
        }
        Transaction transaction = transactionDetails.get();
        if(transaction.getCategorizationStatus().equals(Transaction.CategorizationStatus.CATEGORIZED)) {
            // TODO: Send a message telling user that transaction is already categorized, further update is allowed in app
            TelegramMessageResponse res = telegramClient.sendMessage(messageDetails.chat().id(), UPDATE_NOT_ALLOWED_MSG, null);
            if(!res.ok()) {
                // ADD LOGGING
            }
            return;
        }

        // Create a new category for the user

        Category.CategoryType categoryType = (List.of("INCOME", "REFUND")
                .contains(transaction.getType().toString())) ? Category.CategoryType.INCOME : Category.CategoryType.EXPENSE;
        Category newCategory = new Category(messageDetails.text(), categoryType, userId);
        Category savedCategory = categoryRepository.save(newCategory);

        // Update the category in the transaction
        transaction.setCategoryId(savedCategory.getId());
        transaction.setCategorizationStatus(Transaction.CategorizationStatus.CATEGORIZED);
        transactionRepository.save(transaction);
    }

    private void handleNewChatInit(TelegramUpdateResponse.MessageDetails messageDetails) {
        Long chat_id = messageDetails.chat().id();
        boolean isExistingUser = userRepository.existsByTelegramChatId(chat_id);
        if(isExistingUser) {
            // Reply with a message with existing user
            return;
        }

        String code = generateCode();
        Instant expiresAt = Instant.now().plus(5, ChronoUnit.MINUTES);

        TelegramLink newChatLink = new TelegramLink();
        newChatLink.setChatId(chat_id);
        newChatLink.setCode(code);
        newChatLink.setExpiresAt(expiresAt);
        telegramLinkRepository.save(newChatLink);

        String message = START_MSG + "\n" + code;
        TelegramMessageResponse sendMessageResponse = telegramClient.sendMessage(chat_id, message, null);
        // TODO: Add logging
    }

    private String generateCode() {
        int number = RANDOM.nextInt(900_000) + 100_000; // 100000..999999
        return String.valueOf(number);
    }

    private TelegramMessageRequest generateNewCategorizationMessage(Long chatId, Transaction transaction, List<Category> existingCategories) {
        String type = (List.of("INCOME", "REFUND").contains(transaction.getType().toString())) ? "CREDIT 🟢" : "DEBIT 🔴";

        String textMessage = """
              💳 New Transaction

              Amount: %s %s
              Type: %s
              Receiver: Amazon
              Date: %s

              Please select a category for this transaction 👇
              """.formatted(transaction.getCurrency(), transaction.getAmount(), type, transaction.getTransactionDate());
        InlineKeyboardMarkup categoryButtons = buildCategoryButtons(transaction.getId(), existingCategories);
        return new TelegramMessageRequest(chatId, textMessage, categoryButtons);
    }

    /// Passing UUID directly into InlineKeyboard was exceeding telegram's 64 byte limit.
    /// So we used Base64 encoding on UUID to ensure data is within limit
    private InlineKeyboardMarkup buildCategoryButtons(UUID transactionId, List<Category> categories) {
         List<InlineKeyboardMarkup.InlineKeyboardButton> categoryButtons = categories.stream()
                .map(category ->
                        new InlineKeyboardMarkup.InlineKeyboardButton(
                                category.getName(), "cat:" + compactUuid(transactionId) + ":" + compactUuid(category.getId())
                        )
                )
                .toList();
        List<List<InlineKeyboardMarkup.InlineKeyboardButton>> rows = chunk(categoryButtons, 4);
        return new InlineKeyboardMarkup(rows);
    }

    private static <T> List<List<T>> chunk(List<T> items, int size) {
        List<List<T>> rows = new ArrayList<>();
        for (int i = 0; i < items.size(); i += size) {
            rows.add(items.subList(i, Math.min(i + size, items.size())));
        }
        return rows;
    }

    // Encode: UUID -> compact string
    private static String compactUuid(UUID uuid) {
        ByteBuffer buffer = ByteBuffer.allocate(16);
        buffer.putLong(uuid.getMostSignificantBits());
        buffer.putLong(uuid.getLeastSignificantBits());
        return Base64.getUrlEncoder().withoutPadding().encodeToString(buffer.array());
    }

    // Decode: compact string -> UUID
    private static UUID decompactUuid(String compact) {
        byte[] bytes = Base64.getUrlDecoder().decode(compact);
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        long mostSigBits = buffer.getLong();
        long leastSigBits = buffer.getLong();
        return new UUID(mostSigBits, leastSigBits);
    }

    private boolean isNewCategoryMessageValid(String message) {
        if(message.length() <= 2) {
            return false;
        }
        Matcher matcher = CUSTOM_CATEGORY_PATTERN.matcher(message);
        return matcher.matches();
    }
}
