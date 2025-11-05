package com.example.stockgestion.events;

import java.util.UUID;

public record StockReceivedEvent(UUID productId, UUID WarehouseId, long qtyReceived) {
    
}
