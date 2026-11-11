package com.financetracker.core.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserCreateResponse(
    String message,
    String email,
    String displayName
) {}
