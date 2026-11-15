package com.financetracker.exception;

public class TelegramSendMessageException extends RuntimeException {
    public TelegramSendMessageException(String message) {
        super(message);
    }
    public TelegramSendMessageException() {
        super("Error occurred in bot while sending message");
    }
}
