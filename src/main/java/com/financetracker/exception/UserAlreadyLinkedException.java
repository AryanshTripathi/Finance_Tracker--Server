package com.financetracker.exception;

public class UserAlreadyLinkedException extends RuntimeException {
    public UserAlreadyLinkedException(String message) {
        super(message);
    }
    public UserAlreadyLinkedException() {
        super("User already linked to the telegram. Current app does not support changing telegram ids");
    }
}
