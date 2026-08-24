package com.samir.vortex.bhs.flight.exception;

public class FlightProcessingException extends RuntimeException {

    public FlightProcessingException(String message) {
        super(message);
    }

    public FlightProcessingException(String message, Throwable cause) {
        super(message, cause);
    }
}