package com.example.stockgestion.services.helpers;

import com.example.stockgestion.models.enums.SOStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderStatusHelperTest {

    private OrderStatusHelper orderStatusHelper;

    @BeforeEach
    void setUp() {
        orderStatusHelper = new OrderStatusHelper();
    }

    @Test
    void determineStatus_ShouldReturnPartiallyReserved_WhenHasBackorderAndReserved() {
        // When
        SOStatus result = orderStatusHelper.determineStatus(true, true);

        // Then
        assertEquals(SOStatus.PARTIALLY_RESERVED, result);
    }

    @Test
    void determineStatus_ShouldReturnBackordered_WhenOnlyBackorder() {
        // When
        SOStatus result = orderStatusHelper.determineStatus(true, false);

        // Then
        assertEquals(SOStatus.BACKORDERED, result);
    }

    @Test
    void determineStatus_ShouldReturnReserved_WhenOnlyReserved() {
        // When
        SOStatus result = orderStatusHelper.determineStatus(false, true);

        // Then
        assertEquals(SOStatus.RESERVED, result);
    }

    @Test
    void determineStatus_ShouldReturnReserved_WhenNoBackorderNoReserved() {
        // When
        SOStatus result = orderStatusHelper.determineStatus(false, false);

        // Then
        assertEquals(SOStatus.RESERVED, result);
    }

    @Test
    void determineStatus_ShouldHandleAllCombinations() {
        // Test all 4 possible combinations
        assertEquals(SOStatus.PARTIALLY_RESERVED, orderStatusHelper.determineStatus(true, true));
        assertEquals(SOStatus.BACKORDERED, orderStatusHelper.determineStatus(true, false));
        assertEquals(SOStatus.RESERVED, orderStatusHelper.determineStatus(false, true));
        assertEquals(SOStatus.RESERVED, orderStatusHelper.determineStatus(false, false));
    }
}
