package com.financetracker.exception;

public class UserNotLinkedException extends RuntimeException {
    public UserNotLinkedException(String message) {
        super(message);
    }
    public UserNotLinkedException() {
        super("User not linked to telegram yet. Please connect telegram.");
    }
}
