package com.example.stockgestion.Dto.request;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PurchaseOrderRequestDtoTest {

    @Test
    void noArgsConstructor_ShouldWork() {
        PurchaseOrderRequestDto dto = new PurchaseOrderRequestDto();
        assertNotNull(dto);
    }

    @Test
    void settersAndGetters_ShouldWork() {
        PurchaseOrderRequestDto dto = new PurchaseOrderRequestDto();
        UUID supplierId = UUID.randomUUID();
        
        PurchaseOrderLineRequestDto lineDto = new PurchaseOrderLineRequestDto();
        lineDto.setProductId(UUID.randomUUID());
        lineDto.setQuantity(100);
        
        dto.setSupplierId(supplierId);
        dto.setLines(Arrays.asList(lineDto));

        assertEquals(supplierId, dto.getSupplierId());
        assertNotNull(dto.getLines());
        assertEquals(1, dto.getLines().size());
    }

    @Test
    void supplierId_ShouldBeSettable() {
        PurchaseOrderRequestDto dto = new PurchaseOrderRequestDto();
        UUID id = UUID.randomUUID();
        
        dto.setSupplierId(id);
        assertEquals(id, dto.getSupplierId());
    }
}
