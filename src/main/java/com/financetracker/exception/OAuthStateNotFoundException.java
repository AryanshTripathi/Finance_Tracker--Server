package com.financetracker.exception;

import java.util.UUID;

public class OAuthStateNotFoundException extends RuntimeException {
    public OAuthStateNotFoundException(String message) {
        super(message);
    }
    public OAuthStateNotFoundException(UUID stateId) {
        super("No User found for State: " + stateId);
    }
}
