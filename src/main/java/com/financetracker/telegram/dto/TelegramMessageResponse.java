package com.financetracker.telegram.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TelegramMessageResponse(
    boolean ok,
    ResponseResult result
) {};

record ResponseResult(
        @JsonProperty("message_id")
        Long messageId,
        TelegramUpdateResponse.User from,
        TelegramUpdateResponse.Chat chat,
        Long date,
        String text,
        @JsonProperty("reply_markup")
        InlineKeyboardMarkup replyMarkup
) {};
