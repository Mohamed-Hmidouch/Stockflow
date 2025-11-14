package com.example.stockgestion.Dto.response;

import com.example.stockgestion.models.*;
import com.example.stockgestion.models.enums.SOStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SalesOrderResponseDtoTest {

    @Test
    void constructor_WithSalesOrder_ShouldMapBasicFields() {
        Client client = new Client();
        client.setId(UUID.randomUUID());
        client.setName("Test Client");

        SalesOrder order = new SalesOrder();
        order.setId(UUID.randomUUID());
        order.setClient(client);
        order.setStatus(SOStatus.CREATED);
        order.setTotalPrice(BigDecimal.valueOf(500.00));
        order.setCreatedAt(Instant.now());
        order.setLines(java.util.Collections.emptyList()); // Initialize lines

        SalesOrderResponseDto dto = new SalesOrderResponseDto(order);

        assertNotNull(dto);
        assertEquals(order.getId(), dto.getId());
        assertEquals(order.getStatus(), dto.getStatus());
        assertEquals(order.getTotalPrice(), dto.getTotalPrice());
    }

    @Test
    void noArgsConstructor_ShouldWork() {
        SalesOrderResponseDto dto = new SalesOrderResponseDto();
        assertNotNull(dto);
    }

    @Test
    void settersAndGetters_ShouldWork() {
        SalesOrderResponseDto dto = new SalesOrderResponseDto();
        UUID id = UUID.randomUUID();
        
        dto.setId(id);
        dto.setStatus(SOStatus.RESERVED);
        dto.setTotalPrice(BigDecimal.valueOf(1000.00));

        assertEquals(id, dto.getId());
        assertEquals(SOStatus.RESERVED, dto.getStatus());
        assertEquals(BigDecimal.valueOf(1000.00), dto.getTotalPrice());
    }
}
