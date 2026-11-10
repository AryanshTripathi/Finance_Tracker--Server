package com.financetracker.core.dto;

import jakarta.validation.constraints.NotBlank;

public record UserLoginResponse(
    @NotBlank
    String authToken
) {}