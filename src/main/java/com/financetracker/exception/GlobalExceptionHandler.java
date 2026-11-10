package com.financetracker.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UserAlreadyRegisteredException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmail(UserAlreadyRegisteredException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler({UserNotRegisteredException.class, IncorrectPasswordException.class})
    public ResponseEntity<ErrorResponse> handleBadLogin(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            new ErrorResponse("Invalid email or password")
        );
    }

    @ExceptionHandler(InvalidStringUuidConversionException.class)
    public ResponseEntity<ErrorResponse> handleStringToUuidConversionInJwt(InvalidConversionException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(TelegramGetUpdateException.class)
    public ResponseEntity<ErrorResponse> handleTelegramGetUpdateException(TelegramGetUpdateException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(ex.getMessage()));
    }
}
