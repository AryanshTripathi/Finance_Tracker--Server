package com.financetracker.telegram.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record TelegramUpdateResponse(
   boolean ok,
   List<Result> result
) {
    public record Result(
            @JsonProperty("update_id")
            Long updateId,
            @JsonProperty("callback_query")
            CallbackQuery callbackQuery,
            MessageDetails message
    ) {};

    public record CallbackQuery(
            String id,
            User from,
            CallbackMessage message,
            @JsonProperty("chat_instance")
            String chatInstance,
            String data
    ) {};

    public record User (
            Long id,
            @JsonProperty("is_bot")
            boolean isBot,
            @JsonProperty("first_name")
            String firstName
    ) {};

    public record MessageDetails(
            @JsonProperty("message_id")
            Long messageId,
            User from,
            Chat chat,
            Long date,
            String text,
            @JsonProperty("reply_to_message")
            MessageDetails replyToMessage
    ) {}

    public record CallbackMessage(
            @JsonProperty("message_id")
            Long messageId,
            Chat chat,
            Long date,
            String text
    ) {};

    public record Chat (
            Long id,
            String type
    ) {};
};

