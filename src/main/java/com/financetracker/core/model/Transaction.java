package com.financetracker.core.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
@Getter
@Setter
public class Transaction {
    @Id
    @GeneratedValue
    private UUID id;

    public enum TransactionType {INCOME, EXPENSE, TRANSFER, REFUND};
    public enum CategorizationStatus {UNCATEGORIZED,CATEGORIZED,AUTO_CATEGORIZED};

    private BigDecimal amount;
    private String currency;
    private LocalDate transactionDate;
    private OffsetDateTime transactionTimestamp;
    private String description;
    private String source;
    private String externalRefId;

    @Enumerated(EnumType.STRING)
    private TransactionType type;
    @Enumerated(EnumType.STRING)
    private CategorizationStatus categorizationStatus;

    private UUID userId;
    private UUID categoryId;

    @CreationTimestamp
    private Instant createdAt;
    @UpdateTimestamp
    private Instant updatedAt;

}
