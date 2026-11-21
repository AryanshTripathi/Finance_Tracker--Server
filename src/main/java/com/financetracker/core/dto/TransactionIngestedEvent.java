package com.financetracker.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public record TransactionIngestedEvent(
        @JsonProperty("user_id")
        UUID userId,
        @JsonProperty("transaction_id")
        UUID transactionId
) {
}
