package com.financetracker.telegram.dto;

public record TelegramMessageResponse(
    boolean ok,
    ResponseResult result
) {};

record ResponseResult(
        Long messageId,
        User from,
        Chat chat,
        Long date,
        String text,
        InlineKeyboardMarkup replyMarkup
) {};
