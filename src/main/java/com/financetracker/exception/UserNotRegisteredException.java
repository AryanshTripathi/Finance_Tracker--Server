package com.financetracker.exception;

public class UserNotRegisteredException extends RuntimeException {
    public UserNotRegisteredException(String message) {
        super(message);
    }
    public UserNotRegisteredException() {
        super("User not found");
    }
}
