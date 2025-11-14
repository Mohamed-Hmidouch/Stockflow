package com.example.stockgestion.Dto.response;

import com.example.stockgestion.models.*;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ProductResponseDtoTest {

    @Test
    void constructor_WithProduct_ShouldMapFields() {
        Product product = new Product();
        product.setId(UUID.randomUUID());
        product.setSku("SKU-001");
        product.setName("Test Product");
        product.setPrice(BigDecimal.valueOf(99.99));

        ProductResponseDto dto = new ProductResponseDto(product);

        assertNotNull(dto);
        assertEquals(product.getId(), dto.getId());
        assertEquals(product.getSku(), dto.getSku());
        assertEquals(product.getName(), dto.getName());
        assertEquals(product.getPrice(), dto.getPrice());
    }

    @Test
    void noArgsConstructor_ShouldWork() {
        ProductResponseDto dto = new ProductResponseDto();
        assertNotNull(dto);
    }

    @Test
    void settersAndGetters_ShouldWork() {
        ProductResponseDto dto = new ProductResponseDto();
        UUID id = UUID.randomUUID();
        dto.setId(id);
        dto.setSku("TEST-SKU");
        dto.setName("Test");
        dto.setPrice(BigDecimal.TEN);
        dto.setCategory("Cat");

        assertEquals(id, dto.getId());
        assertEquals("TEST-SKU", dto.getSku());
        assertEquals("Test", dto.getName());
        assertEquals(BigDecimal.TEN, dto.getPrice());
        assertEquals("Cat", dto.getCategory());
    }
}
