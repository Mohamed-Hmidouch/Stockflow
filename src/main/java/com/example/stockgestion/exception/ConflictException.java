package com.example.stockgestion.exception;

/** Exception utilisée pour signaler un conflit (HTTP 409). */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) {
        super(message);
    }
}
