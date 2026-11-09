package com.financetracker.telegram.dto;

public record TelegramMessageRequest(
        Long chatId,
        String text,
        InlineKeyboardMarkup replyMarkup
) {};

;