package com.example.stockgestion.Dto.request;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PurchaseOrderLineRequestDtoTest {

    @Test
    void noArgsConstructor_ShouldWork() {
        PurchaseOrderLineRequestDto dto = new PurchaseOrderLineRequestDto();
        assertNotNull(dto);
    }

    @Test
    void allArgsConstructor_ShouldWork() {
        UUID productId = UUID.randomUUID();
        
        PurchaseOrderLineRequestDto dto = new PurchaseOrderLineRequestDto(
                productId,
                100,
                BigDecimal.valueOf(25.50)
        );

        assertEquals(productId, dto.getProductId());
        assertEquals(100, dto.getQuantity());
        assertEquals(BigDecimal.valueOf(25.50), dto.getUnitPrice());
    }

    @Test
    void settersAndGetters_ShouldWork() {
        PurchaseOrderLineRequestDto dto = new PurchaseOrderLineRequestDto();
        UUID productId = UUID.randomUUID();
        
        dto.setProductId(productId);
        dto.setQuantity(200);
        dto.setUnitPrice(BigDecimal.valueOf(15.75));

        assertEquals(productId, dto.getProductId());
        assertEquals(200, dto.getQuantity());
        assertEquals(BigDecimal.valueOf(15.75), dto.getUnitPrice());
    }
}
