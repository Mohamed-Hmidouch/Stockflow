package com.example.stockgestion.Dto.request;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ReceivedLineDtoTest {

    @Test
    void noArgsConstructor_ShouldWork() {
        ReceivedLineDto dto = new ReceivedLineDto();
        assertNotNull(dto);
    }

    @Test
    void settersAndGetters_ShouldWork() {
        ReceivedLineDto dto = new ReceivedLineDto();
        UUID productId = UUID.randomUUID();
        UUID poLineId = UUID.randomUUID();
        
        dto.setProductId(productId);
        dto.setPoLine(poLineId);
        dto.setQuantityReceived(150);

        assertEquals(productId, dto.getProductId());
        assertEquals(poLineId, dto.getPoLine());
        assertEquals(150, dto.getQuantityReceived());
    }

    @Test
    void setQuantityReceived_ShouldWork() {
        ReceivedLineDto dto = new ReceivedLineDto();
        dto.setQuantityReceived(0);
        assertEquals(0, dto.getQuantityReceived());
        
        dto.setQuantityReceived(999);
        assertEquals(999, dto.getQuantityReceived());
    }
}
