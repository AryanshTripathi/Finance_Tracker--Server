package com.financetracker.exception;

import java.util.UUID;

public class CategoryNotFoundException extends RuntimeException {
    public CategoryNotFoundException(String message) {
        super(message);
    }
    public CategoryNotFoundException(UUID id) {
        super("Category not found for Id: + " + id);
    }
}
