package com.financetracker.gmail.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GmailTokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("refresh_token") String refreshToken,
        @JsonProperty("expires_in") Long expiresIn,
        String scope,
        @JsonProperty("token_type") String tokenType
) {}
