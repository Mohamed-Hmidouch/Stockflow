package com.example.stockgestion.exception.dto;

import java.time.Instant;
import java.util.Map;

/**
 * Représente une réponse d'erreur de validation contenant les erreurs par champ.
 */
public class ValidationErrorResponse extends ErrorResponse {

    private Map<String, String> validationErrors;

    public ValidationErrorResponse() {
        super();
    }

    public ValidationErrorResponse(int status, String message, String path, Instant timestamp,
                                   Map<String, String> validationErrors) {
        super(status, message, path, timestamp);
        this.validationErrors = validationErrors;
    }

    public Map<String, String> getValidationErrors() {
        return validationErrors;
    }

    public void setValidationErrors(Map<String, String> validationErrors) {
        this.validationErrors = validationErrors;
    }
}
