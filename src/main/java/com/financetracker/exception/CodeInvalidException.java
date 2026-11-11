package com.financetracker.exception;

public class CodeInvalidException extends RuntimeException {
    public CodeInvalidException(String message) {
        super(message);
    }
    public CodeInvalidException() {
        super("Code Not Valid. Please get a new code from bot");
    }
}
