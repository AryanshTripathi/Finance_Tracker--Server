package com.financetracker.telegram.service;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import com.financetracker.core.model.User;
import com.financetracker.core.repository.UserRepository;
import com.financetracker.exception.*;
import com.financetracker.telegram.TelegramClient;
import com.financetracker.telegram.dto.TelegramLinkRequest;
import com.financetracker.telegram.dto.TelegramMessageResponse;
import com.financetracker.telegram.dto.TelegramUpdateResponse;
import com.financetracker.telegram.model.BotOffset;
import com.financetracker.telegram.model.TelegramLink;
import com.financetracker.telegram.repository.BotOffsetRepository;
import com.financetracker.telegram.repository.TelegramLinkRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class TelegramPollingService {
    private final TelegramClient telegramClient;
    private final UserRepository userRepository;
    private final BotOffsetRepository botOffsetRepository;
    private final TelegramLinkRepository telegramLinkRepository;

    private static final SecureRandom RANDOM = new SecureRandom();

    private static final String START_CMD = "/start";
    private static final String START_MSG = "Paste this Code on the Web App to validate the chat with the Bot";
    private static final String AUTH_SUCCESS_MSG = "Account Authentication Successful. You will now receive updates about your daily transactions";

    public TelegramPollingService(TelegramClient telegramClient, UserRepository userRepository, BotOffsetRepository botOffsetRepository, TelegramLinkRepository telegramLinkRepository) {
        this.telegramClient = telegramClient;
        this.userRepository = userRepository;
        this.botOffsetRepository = botOffsetRepository;
        this.telegramLinkRepository = telegramLinkRepository;
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
            throw new TelegramGetUpdateException();
        }
        if(updates.result().isEmpty()) {
            // ADD LOGGING
            return;
        }

        Long newLastOffset = handleBotUpdate(updates);
        offsetRecord.setLastOffset(newLastOffset);
        botOffsetRepository.save(offsetRecord);
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
                // Update category of the transaction
            } else if(result.message() != null && result.message().text() != null) {
                if(result.message().text().startsWith("/")) {
                    //A command: To be processed separately
                    if (result.message().text().equals(START_CMD)) {
                        handleNewChatInit(result.message());
                    }
                } else {
                    // Handle message replies
                }
                // Create new category and Update category of the transaction
            }
            // Add logging for malformed or non-text input
        }
        return maxUpdateOffset + 1;
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
        // Log the response
    }

    private static String generateCode() {
        int number = RANDOM.nextInt(900_000) + 100_000; // 100000..999999
        return String.valueOf(number);
    }
}
