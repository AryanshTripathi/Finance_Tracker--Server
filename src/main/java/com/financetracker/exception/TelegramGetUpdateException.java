package com.financetracker.exception;

public class TelegramGetUpdateException extends RuntimeException {
    public TelegramGetUpdateException() {
        super("Error fetching getUpdate response from telegram");
    }
    public TelegramGetUpdateException(String message) {
        super(message);
    }
}
