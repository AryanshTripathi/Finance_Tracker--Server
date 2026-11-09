package com.financetracker.telegram;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record InlineKeyboardMarkup(
        List<List<InlineKeyboardButton>> inlineKeyboard
) {}

record InlineKeyboardButton(
        String text,
        @JsonProperty("callback_data")
        String callbackData
) {}