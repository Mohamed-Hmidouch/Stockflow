package com.example.stockgestion.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ResourceNotFoundExceptionTest {

    @Test
    void constructor_WithMessage_ShouldSetMessage() {
        String message = "Resource not found";
        ResourceNotFoundException exception = new ResourceNotFoundException(message);
        
        assertEquals(message, exception.getMessage());
    }

    @Test
    void constructor_ShouldBeRuntimeException() {
        ResourceNotFoundException exception = new ResourceNotFoundException("Test");
        
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void getMessage_ShouldReturnCorrectMessage() {
        ResourceNotFoundException exception = new ResourceNotFoundException("Product not found");
        
        assertEquals("Product not found", exception.getMessage());
    }
}
