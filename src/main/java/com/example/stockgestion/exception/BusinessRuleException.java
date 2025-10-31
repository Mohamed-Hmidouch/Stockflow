package com.example.stockgestion.exception;

/** Exception utilisée pour signaler une violation d'une règle métier (HTTP 400). */
public class BusinessRuleException extends RuntimeException {
    public BusinessRuleException(String message) {
        super(message);
    }
}
