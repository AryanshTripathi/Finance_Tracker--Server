package com.financetracker.exception;

public class IncorrectPasswordException extends RuntimeException {
    public IncorrectPasswordException() {
        super("Incorrect Password Exception");
    }
    public IncorrectPasswordException(String message) {
        super(message);
    }
}
