package com.example.stockgestion.Dto.response;

import com.example.stockgestion.models.*;
import com.example.stockgestion.models.enums.POStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PurchaseOrderResponseDtoTest {

    @Test
    void constructor_WithPurchaseOrder_ShouldMapBasicFields() {
        Supplier supplier = new Supplier();
        supplier.setId(UUID.randomUUID());
        supplier.setName("Test Supplier");

        PurchaseOrder order = new PurchaseOrder();
        order.setId(UUID.randomUUID());
        order.setSupplier(supplier);
        order.setStatus(POStatus.APPROVED);
        order.setCreatedAt(Instant.now());
        order.setLines(java.util.Collections.emptyList()); // Initialize lines

        PurchaseOrderResponseDto dto = new PurchaseOrderResponseDto(order);

        assertNotNull(dto);
        assertEquals(order.getId(), dto.getId());
        assertEquals(order.getStatus(), dto.getStatus());
    }

    @Test
    void noArgsConstructor_ShouldWork() {
        PurchaseOrderResponseDto dto = new PurchaseOrderResponseDto();
        assertNotNull(dto);
    }

    @Test
    void settersAndGetters_ShouldWork() {
        PurchaseOrderResponseDto dto = new PurchaseOrderResponseDto();
        UUID id = UUID.randomUUID();
        
        dto.setId(id);
        dto.setStatus(POStatus.RECEIVED);

        assertEquals(id, dto.getId());
        assertEquals(POStatus.RECEIVED, dto.getStatus());
    }
}
