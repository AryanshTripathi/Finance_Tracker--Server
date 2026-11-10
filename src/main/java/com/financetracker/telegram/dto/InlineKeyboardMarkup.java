package com.financetracker.telegram.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record InlineKeyboardMarkup(
        @JsonProperty("inline_keyboard")
        List<List<InlineKeyboardButton>> inlineKeyboard
) {}

record InlineKeyboardButton(
        String text,
        @JsonProperty("callback_data")
        String callbackData
) {}