package com.financetracker.telegram.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.validator.constraints.Length;

import java.time.Instant;

@Entity
@Getter
@Setter
@Table(name = "telegram_link_request")
public class TelegramLink {
    @Id
    private String code;

    private Long chatId;
    private Instant expiresAt;

    @CreationTimestamp
    private Instant createdAt;
}
