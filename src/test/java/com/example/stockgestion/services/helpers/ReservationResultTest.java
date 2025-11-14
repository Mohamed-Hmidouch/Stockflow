package com.example.stockgestion.services.helpers;

import com.example.stockgestion.models.SalesOrderLine;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ReservationResultTest {

    @Test
    void constructor_ShouldSetAllFields() {
        // Given
        List<SalesOrderLine> lines = new ArrayList<>();
        boolean hasReserved = true;
        boolean hasBackorder = false;

        // When
        ReservationResult result = new ReservationResult(lines, hasReserved, hasBackorder);

        // Then
        assertNotNull(result);
        assertEquals(lines, result.getLines());
        assertTrue(result.isHasReserved());
        assertFalse(result.isHasBackorder());
    }

    @Test
    void getters_ShouldReturnCorrectValues() {
        // Given
        SalesOrderLine line1 = new SalesOrderLine();
        SalesOrderLine line2 = new SalesOrderLine();
        List<SalesOrderLine> lines = Arrays.asList(line1, line2);
        
        ReservationResult result = new ReservationResult(lines, false, true);

        // When & Then
        assertEquals(2, result.getLines().size());
        assertFalse(result.isHasReserved());
        assertTrue(result.isHasBackorder());
    }

    @Test
    void constructor_ShouldHandleEmptyLines() {
        // Given
        List<SalesOrderLine> emptyLines = new ArrayList<>();

        // When
        ReservationResult result = new ReservationResult(emptyLines, false, false);

        // Then
        assertNotNull(result.getLines());
        assertTrue(result.getLines().isEmpty());
        assertFalse(result.isHasReserved());
        assertFalse(result.isHasBackorder());
    }

    @Test
    void constructor_ShouldHandleBothFlags() {
        // Given
        List<SalesOrderLine> lines = Arrays.asList(new SalesOrderLine(), new SalesOrderLine());

        // When
        ReservationResult result = new ReservationResult(lines, true, true);

        // Then
        assertTrue(result.isHasReserved());
        assertTrue(result.isHasBackorder());
    }
}
