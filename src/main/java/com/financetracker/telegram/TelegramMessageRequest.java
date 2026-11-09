package com.financetracker.telegram;

public record TelegramMessageRequest(
        Long chatId,
        String text,
        InlineKeyboardMarkup replyMarkup
) {};

;