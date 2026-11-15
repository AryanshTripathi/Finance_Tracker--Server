package com.financetracker.telegram.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
public class TelegramMessageTransaction {
    @Id
    @GeneratedValue
    private UUID id;

    private Long chatId;
    private Long messageId;
    private UUID transactionId;

    @CreationTimestamp
    private Instant createdAt;
    @UpdateTimestamp
    private Instant updatedAt;

    public TelegramMessageTransaction() {}

    public TelegramMessageTransaction(Long chatId, Long messageId, UUID transactionId) {
        this.chatId = chatId;
        this.messageId = messageId;
        this.transactionId = transactionId;
    }
}
