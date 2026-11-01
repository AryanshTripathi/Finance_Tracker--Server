package com.financetracker.exception;

public class InvalidStringUuidConversionException extends InvalidConversionException {
    public InvalidStringUuidConversionException(String message) {
        super(message);
    }

    public InvalidStringUuidConversionException(String dataType1, String dataType2) {
        super("Invalid Conversion from " + dataType1 + " to " + dataType2 + " Occurred" );
    }
}