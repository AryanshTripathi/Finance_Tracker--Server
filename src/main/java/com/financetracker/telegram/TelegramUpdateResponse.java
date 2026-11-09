package com.financetracker.telegram;

import java.util.List;

public record TelegramUpdateResponse(
   boolean ok,
   List<Result> result
) {};

record Result(
    Long updateId,
    CallbackQuery callbackQuery,
    MessageDetails message
) {};

record CallbackQuery(
    String id,
    User from,
    CallbackMessage message,
    String chatInstance,
    String data
) {};

record User (
    Long id,
    boolean isBot,
    String firstName
) {};

record MessageDetails(
    Long messageId,
    User from,
    Chat chat,
    Long date,
    String text
) {}

record CallbackMessage(
    Long messageId,
    Chat chat,
    Long date,
    String text
) {};

record Chat (
    Long id,
    String type
) {};