package com.financetracker.core.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "categories")
@Getter
@Setter
public class Category {
    @Id
    @GeneratedValue
    private UUID id;

    public enum CategoryType {INCOME, EXPENSE};

    private String name;
    @Enumerated(EnumType.STRING)
    private CategoryType type;
    private boolean isDefault;
    @CreationTimestamp
    private Instant createdAt;
    @UpdateTimestamp
    private Instant updatedAt;

    @Column(name = "owner_user_id")
    private UUID userId;
}
