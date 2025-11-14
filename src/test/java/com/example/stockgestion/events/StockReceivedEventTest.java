package com.example.stockgestion.events;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class StockReceivedEventTest {

    @Test
    void constructor_ShouldCreateEventWithAllFields() {
        UUID productId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        long quantity = 100;

        StockReceivedEvent event = new StockReceivedEvent(productId, warehouseId, quantity);

        assertNotNull(event);
        assertEquals(productId, event.productId());
        assertEquals(warehouseId, event.WarehouseId());
        assertEquals(quantity, event.qtyReceived());
    }

    @Test
    void getters_ShouldReturnCorrectValues() {
        UUID productId = UUID.randomUUID();
        UUID warehouseId = UUID.randomUUID();
        long quantity = 250;

        StockReceivedEvent event = new StockReceivedEvent(productId, warehouseId, quantity);

        assertEquals(productId, event.productId());
        assertEquals(warehouseId, event.WarehouseId());
        assertEquals(quantity, event.qtyReceived());
    }

    @Test
    void event_WithDifferentQuantities_ShouldWork() {
        StockReceivedEvent event1 = new StockReceivedEvent(UUID.randomUUID(), UUID.randomUUID(), 0);
        StockReceivedEvent event2 = new StockReceivedEvent(UUID.randomUUID(), UUID.randomUUID(), 9999);

        assertEquals(0, event1.qtyReceived());
        assertEquals(9999, event2.qtyReceived());
    }
}
