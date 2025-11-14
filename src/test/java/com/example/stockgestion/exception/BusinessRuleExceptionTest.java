package com.example.stockgestion.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class BusinessRuleExceptionTest {

    @Test
    void constructor_WithMessage_ShouldSetMessage() {
        String message = "Business rule violated";
        BusinessRuleException exception = new BusinessRuleException(message);
        
        assertEquals(message, exception.getMessage());
    }

    @Test
    void constructor_ShouldBeRuntimeException() {
        BusinessRuleException exception = new BusinessRuleException("Test");
        
        assertTrue(exception instanceof RuntimeException);
    }

    @Test
    void getMessage_ShouldReturnCorrectMessage() {
        BusinessRuleException exception = new BusinessRuleException("Insufficient stock");
        
        assertEquals("Insufficient stock", exception.getMessage());
    }
}
