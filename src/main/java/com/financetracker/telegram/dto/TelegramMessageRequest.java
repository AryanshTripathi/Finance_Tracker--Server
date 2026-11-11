package com.financetracker.telegram.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record TelegramMessageRequest(
        @JsonProperty("chat_id")
        Long chatId,
        String text,
        @JsonProperty("reply_markup")
        InlineKeyboardMarkup replyMarkup
) {};