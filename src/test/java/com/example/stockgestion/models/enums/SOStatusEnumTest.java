package com.example.stockgestion.models.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SOStatusEnumTest {

    @Test
    void values_ShouldContainAllStatuses() {
        SOStatus[] statuses = SOStatus.values();
        
        assertNotNull(statuses);
        assertEquals(7, statuses.length); // Mise à jour: 7 statuts maintenant (ajout de SHIPPED)
    }

    @Test
    void valueOf_ShouldWork() {
        assertEquals(SOStatus.CREATED, SOStatus.valueOf("CREATED"));
        assertEquals(SOStatus.RESERVED, SOStatus.valueOf("RESERVED"));
        assertEquals(SOStatus.PARTIALLY_RESERVED, SOStatus.valueOf("PARTIALLY_RESERVED"));
        assertEquals(SOStatus.BACKORDERED, SOStatus.valueOf("BACKORDERED"));
        assertEquals(SOStatus.SHIPPED, SOStatus.valueOf("SHIPPED")); // Nouveau statut
        assertEquals(SOStatus.DELIVERED, SOStatus.valueOf("DELIVERED"));
        assertEquals(SOStatus.CANCELED, SOStatus.valueOf("CANCELED"));
    }

    @Test
    void name_ShouldReturnCorrectString() {
        assertEquals("CREATED", SOStatus.CREATED.name());
        assertEquals("RESERVED", SOStatus.RESERVED.name());
        assertEquals("PARTIALLY_RESERVED", SOStatus.PARTIALLY_RESERVED.name());
        assertEquals("BACKORDERED", SOStatus.BACKORDERED.name());
        assertEquals("SHIPPED", SOStatus.SHIPPED.name()); // Nouveau statut
        assertEquals("DELIVERED", SOStatus.DELIVERED.name());
        assertEquals("CANCELED", SOStatus.CANCELED.name());
    }
}
