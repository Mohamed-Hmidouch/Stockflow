package com.example.stockgestion.services.helpers;

import com.example.stockgestion.models.Client;
import com.example.stockgestion.models.SalesOrder;
import com.example.stockgestion.models.SalesOrderLine;
import com.example.stockgestion.models.enums.SOStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SalesOrderBuilderTest {

    private SalesOrderBuilder salesOrderBuilder;
    private Client client;

    @BeforeEach
    void setUp() {
        salesOrderBuilder = new SalesOrderBuilder();
        
        client = new Client();
        client.setId(UUID.randomUUID());
        client.setName("Test Client");
    }

    @Test
    void initialize_ShouldCreateNewOrder_WithCorrectProperties() {
        // When
        SalesOrder result = salesOrderBuilder.initialize(client);

        // Then
        assertNotNull(result);
        assertEquals(client, result.getClient());
        assertEquals(SOStatus.CREATED, result.getStatus());
        assertNotNull(result.getCreatedAt());
    }

    @Test
    void initialize_ShouldCreateDifferentInstances() {
        // When
        SalesOrder order1 = salesOrderBuilder.initialize(client);
        SalesOrder order2 = salesOrderBuilder.initialize(client);

        // Then
        assertNotSame(order1, order2);
    }

    @Test
    void finalize_ShouldSetAllProperties() {
        // Given
        SalesOrder order = new SalesOrder();
        SalesOrderLine line1 = new SalesOrderLine();
        SalesOrderLine line2 = new SalesOrderLine();
        List<SalesOrderLine> lines = Arrays.asList(line1, line2);
        BigDecimal totalPrice = BigDecimal.valueOf(500.00);
        SOStatus status = SOStatus.RESERVED;

        // When
        salesOrderBuilder.finalize(order, lines, totalPrice, status);

        // Then
        assertEquals(status, order.getStatus());
        assertEquals(lines, order.getLines());
        assertEquals(totalPrice, order.getTotalPrice());
        assertEquals(2, order.getLines().size());
    }

    @Test
    void finalize_ShouldHandleEmptyLines() {
        // Given
        SalesOrder order = new SalesOrder();
        List<SalesOrderLine> lines = new ArrayList<>();
        BigDecimal totalPrice = BigDecimal.ZERO;

        // When
        salesOrderBuilder.finalize(order, lines, totalPrice, SOStatus.BACKORDERED);

        // Then
        assertEquals(SOStatus.BACKORDERED, order.getStatus());
        assertTrue(order.getLines().isEmpty());
        assertEquals(BigDecimal.ZERO, order.getTotalPrice());
    }

    @Test
    void finalize_ShouldHandlePartiallyReservedStatus() {
        // Given
        SalesOrder order = new SalesOrder();
        List<SalesOrderLine> lines = Arrays.asList(new SalesOrderLine());
        BigDecimal totalPrice = BigDecimal.valueOf(1000.00);

        // When
        salesOrderBuilder.finalize(order, lines, totalPrice, SOStatus.PARTIALLY_RESERVED);

        // Then
        assertEquals(SOStatus.PARTIALLY_RESERVED, order.getStatus());
        assertEquals(1, order.getLines().size());
    }

    @Test
    void initialize_AndFinalize_ShouldWorkTogether() {
        // Given
        SalesOrderLine line = new SalesOrderLine();
        List<SalesOrderLine> lines = Arrays.asList(line);
        BigDecimal totalPrice = BigDecimal.valueOf(250.00);

        // When
        SalesOrder order = salesOrderBuilder.initialize(client);
        salesOrderBuilder.finalize(order, lines, totalPrice, SOStatus.RESERVED);

        // Then
        assertEquals(client, order.getClient());
        assertEquals(SOStatus.RESERVED, order.getStatus());
        assertEquals(lines, order.getLines());
        assertEquals(totalPrice, order.getTotalPrice());
        assertNotNull(order.getCreatedAt());
    }
}
