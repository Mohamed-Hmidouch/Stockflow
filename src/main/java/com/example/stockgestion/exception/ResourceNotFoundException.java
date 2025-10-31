package com.example.stockgestion.exception;

/**
 * Exception levée lorsque une ressource demandée n'existe pas.
 * Publique pour être utilisable depuis n'importe quel package.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resource, String field, Object value) {
        super(String.format("%s non trouvé avec %s: '%s'", resource, field, value));
    }
}
