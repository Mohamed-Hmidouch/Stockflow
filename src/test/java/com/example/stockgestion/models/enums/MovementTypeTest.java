package com.example.stockgestion.models.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MovementTypeTest {

    @Test
    void values_ShouldReturnAllTypes() {
        MovementType[] types = MovementType.values();
        
        assertNotNull(types);
        assertTrue(types.length >= 2);
    }

    @Test
    void valueOf_ShouldReturnCorrectEnum() {
        assertEquals(MovementType.INBOUND, MovementType.valueOf("INBOUND"));
        assertEquals(MovementType.OUTBOUND, MovementType.valueOf("OUTBOUND"));
    }

    @Test
    void name_ShouldReturnCorrectName() {
        assertEquals("INBOUND", MovementType.INBOUND.name());
        assertEquals("OUTBOUND", MovementType.OUTBOUND.name());
    }

    @Test
    void toString_ShouldWork() {
        assertNotNull(MovementType.INBOUND.toString());
        assertNotNull(MovementType.OUTBOUND.toString());
    }
}
