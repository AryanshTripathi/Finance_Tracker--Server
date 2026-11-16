package com.financetracker.gmail.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "gmail_account_link")
public class GmailLink {
    @Id
    UUID id;

    UUID userId;
    Instant expiresAt;

    @CreationTimestamp
    Instant createdAt;

    public GmailLink() {}

    public GmailLink(UUID userId, Instant expiresAt) {
        this.id = UUID.randomUUID();
        this.userId = userId;
        this.expiresAt = expiresAt;
    }
}
