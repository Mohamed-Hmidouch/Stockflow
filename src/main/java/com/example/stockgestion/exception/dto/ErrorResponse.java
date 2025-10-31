package com.example.stockgestion.exception.dto;

import java.time.Instant;

/**
 * Représente une réponse d'erreur standardisée pour les contrôleurs REST.
 */
public class ErrorResponse {
    private int status;
    private String message;
    private String path;
    private Instant timestamp;

    public ErrorResponse() {
    }

    public ErrorResponse(int status, String message, String path, Instant timestamp) {
        this.status = status;
        this.message = message;
        this.path = path;
        this.timestamp = timestamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}
