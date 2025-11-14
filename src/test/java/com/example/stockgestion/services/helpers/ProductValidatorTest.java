package com.example.stockgestion.services.helpers;

import com.example.stockgestion.exception.BusinessRuleException;
import com.example.stockgestion.exception.ResourceNotFoundException;
import com.example.stockgestion.models.Product;
import com.example.stockgestion.repositories.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductValidatorTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductValidator productValidator;

    private UUID productId;
    private Product product;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        product = new Product();
        product.setId(productId);
        product.setName("Test Product");
        product.setSku("TEST-001");
        product.setPrice(BigDecimal.valueOf(100.00));
    }

    @Test
    void validateAndGet_ShouldReturnProduct_WhenProductExists() {
        // Given
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // When
        Product result = productValidator.validateAndGet(productId);

        // Then
        assertNotNull(result);
        assertEquals(productId, result.getId());
        assertEquals("Test Product", result.getName());
        verify(productRepository).findById(productId);
    }

    @Test
    void validateAndGet_ShouldThrowException_WhenProductNotFound() {
        // Given
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> productValidator.validateAndGet(productId)
        );

        assertTrue(exception.getMessage().contains("Product"));
        assertTrue(exception.getMessage().contains("id"));
        verify(productRepository).findById(productId);
    }

    @Test
    void validateAndGetPrice_ShouldReturnPrice_WhenPriceIsSet() {
        // Given - product already has price set in setUp()

        // When
        BigDecimal result = productValidator.validateAndGetPrice(product);

        // Then
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(100.00), result);
    }

    @Test
    void validateAndGetPrice_ShouldThrowException_WhenPriceIsNull() {
        // Given
        product.setPrice(null);

        // When & Then
        BusinessRuleException exception = assertThrows(
            BusinessRuleException.class,
            () -> productValidator.validateAndGetPrice(product)
        );

        assertTrue(exception.getMessage().contains("n'a pas de prix défini"));
        assertTrue(exception.getMessage().contains(product.getName()));
    }

    @Test
    void validateAndGetPrice_ShouldHandleZeroPrice() {
        // Given
        product.setPrice(BigDecimal.ZERO);

        // When
        BigDecimal result = productValidator.validateAndGetPrice(product);

        // Then
        assertNotNull(result);
        assertEquals(BigDecimal.ZERO, result);
    }

    @Test
    void validateAndGetPrice_ShouldHandleNegativePrice() {
        // Given
        product.setPrice(BigDecimal.valueOf(-50.00));

        // When
        BigDecimal result = productValidator.validateAndGetPrice(product);

        // Then
        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(-50.00), result);
    }
}
