package com.financetracker.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({UserAlreadyRegisteredException.class, UserAlreadyLinkedException.class})
    public ResponseEntity<ErrorResponse> handleDuplicateEmail(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler({UserNotRegisteredException.class, IncorrectPasswordException.class})
    public ResponseEntity<ErrorResponse> handleBadLogin(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            new ErrorResponse("Invalid email or password")
        );
    }

    @ExceptionHandler({InvalidStringUuidConversionException.class, TelegramGetUpdateException.class})
    public ResponseEntity<ErrorResponse> handleStringToUuidConversionInJwt(RuntimeException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(CodeInvalidException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCode(CodeInvalidException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(ex.getMessage()));
    }

    @ExceptionHandler(CodeExpiredException.class)
    public ResponseEntity<ErrorResponse> handleExpiredCode(CodeExpiredException ex) {
        return ResponseEntity.status(HttpStatus.GONE).body(new ErrorResponse(ex.getMessage()));
    }
}
