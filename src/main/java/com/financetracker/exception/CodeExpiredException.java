package com.financetracker.exception;

public class CodeExpiredException extends RuntimeException {
    public CodeExpiredException(String message) {
        super(message);
    }
    public CodeExpiredException() {
        super("Telegram Code Expired. Please get a new code from bot and paste here within 5 minutes");
    }
}
