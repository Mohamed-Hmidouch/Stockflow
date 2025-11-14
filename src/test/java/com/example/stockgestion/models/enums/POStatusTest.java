package com.example.stockgestion.models.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class POStatusTest {

    @Test
    void values_ShouldReturnAllStatuses() {
        POStatus[] statuses = POStatus.values();
        
        assertNotNull(statuses);
        assertTrue(statuses.length >= 5);
    }

    @Test
    void valueOf_ShouldReturnCorrectEnum() {
        assertEquals(POStatus.DRAFT, POStatus.valueOf("DRAFT"));
        assertEquals(POStatus.APPROVED, POStatus.valueOf("APPROVED"));
        assertEquals(POStatus.RECEIVED, POStatus.valueOf("RECEIVED"));
        assertEquals(POStatus.CANCELED, POStatus.valueOf("CANCELED"));
    }

    @Test
    void name_ShouldReturnCorrectName() {
        assertEquals("DRAFT", POStatus.DRAFT.name());
        assertEquals("APPROVED", POStatus.APPROVED.name());
        assertEquals("RECEIVED", POStatus.RECEIVED.name());
    }
}
