package com.financetracker.telegram.repository;

import com.financetracker.telegram.model.TelegramMessageTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TelegramMessageTransactionRepository extends JpaRepository<TelegramMessageTransaction, UUID> {
    Optional<TelegramMessageTransaction> findByChatIdAndMessageId(Long chatId, Long messageId);
}
