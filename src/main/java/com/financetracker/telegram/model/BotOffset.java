package com.financetracker.telegram.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "telegram_bot_poll_state")
public class BotOffset {
    public BotOffset() {}
    public BotOffset(Long lastOffset) {
        this.lastOffset = lastOffset;
    }

    @Id
    @GeneratedValue
    private UUID id;
    private Long lastOffset;

    @CreationTimestamp
    private Instant createdAt;
    @UpdateTimestamp
    private Instant updatedAt;
}
