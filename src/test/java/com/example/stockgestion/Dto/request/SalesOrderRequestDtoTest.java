package com.example.stockgestion.Dto.request;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SalesOrderRequestDtoTest {

    @Test
    void noArgsConstructor_ShouldWork() {
        SalesOrderRequestDto dto = new SalesOrderRequestDto();
        assertNotNull(dto);
    }

    @Test
    void settersAndGetters_ShouldWork() {
        SalesOrderRequestDto dto = new SalesOrderRequestDto();
        UUID clientId = UUID.randomUUID();
        
        SalesOrderLineRequestDto lineDto = new SalesOrderLineRequestDto();
        lineDto.setProductId(UUID.randomUUID());
        lineDto.setQuantity(100L);
        
        dto.setClientId(clientId);
        dto.setLines(Arrays.asList(lineDto));

        assertEquals(clientId, dto.getClientId());
        assertNotNull(dto.getLines());
        assertEquals(1, dto.getLines().size());
    }

    @Test
    void clientId_ShouldBeSettable() {
        SalesOrderRequestDto dto = new SalesOrderRequestDto();
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        
        dto.setClientId(id1);
        assertEquals(id1, dto.getClientId());
        
        dto.setClientId(id2);
        assertEquals(id2, dto.getClientId());
    }
}
