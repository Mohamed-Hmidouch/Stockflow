package com.example.stockgestion.services.helpers;

import com.example.stockgestion.exception.ResourceNotFoundException;
import com.example.stockgestion.models.Inventory;
import com.example.stockgestion.models.Product;
import com.example.stockgestion.models.SalesOrder;
import com.example.stockgestion.models.SalesOrderLine;
import com.example.stockgestion.models.WareHouse;
import com.example.stockgestion.repositories.InventoryRepository;
import com.example.stockgestion.repositories.WareHouseRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
@AllArgsConstructor
public class StockReservationHelper {
    private final InventoryRepository inventoryRepository;
    private final WareHouseRepository wareHouseRepository;


    public List<Inventory> getInventoriesSortedByAvailability(Product product) {
        List<Inventory> inventories = inventoryRepository.findByProduct_Id(product.getId());
        inventories.sort(Comparator.comparingLong(inv -> 
            -(inv.getQtyOnHand() - inv.getQtyReserved())
        ));
        return inventories;
    }


    public ReservationResult reserveAcrossWarehouses(
            SalesOrder order,
            Product product,
            BigDecimal unitPrice,
            long requestedQty,
            List<Inventory> inventories,
            List<Inventory> inventoriesToUpdate
    ) {
        List<SalesOrderLine> lines = new ArrayList<>();
        long remainingQty = requestedQty;
        boolean hasReserved = false;

        // Réserver dans chaque warehouse disponible
        for (Inventory inventory : inventories) {
            if (remainingQty <= 0) break;
            
            long availableQty = calculateAvailableQty(inventory);
            if (availableQty <= 0) continue;

            long qtyToReserve = Math.min(availableQty, remainingQty);
            
            reserveInventory(inventory, qtyToReserve, inventoriesToUpdate);
            SalesOrderLine line = buildReservedLine(order, product, inventory.getWarehouse(), 
                                                     qtyToReserve, unitPrice);
            lines.add(line);
            
            remainingQty -= qtyToReserve;
            hasReserved = true;
        }

        // Créer backorder si stock insuffisant
        boolean hasBackorder = false;
        if (remainingQty > 0) {
            WareHouse fallbackWarehouse = findFallbackWarehouse(inventories);
            SalesOrderLine backorderLine = buildBackorderLine(order, product, fallbackWarehouse, 
                                                              remainingQty, unitPrice);
            lines.add(backorderLine);
            hasBackorder = true;
        }

        return new ReservationResult(lines, hasReserved, hasBackorder);
    }


    public long calculateAvailableQty(Inventory inventory) {
        return inventory.getQtyOnHand() - inventory.getQtyReserved();
    }


    public void reserveInventory(Inventory inventory, long qty, List<Inventory> inventoriesToUpdate) {
        inventory.setQtyReserved(inventory.getQtyReserved() + qty);
        inventoriesToUpdate.add(inventory);
    }


    public SalesOrderLine buildReservedLine(SalesOrder order, Product product, WareHouse warehouse,
                                            long qty, BigDecimal unitPrice) {
        return SalesOrderLine.builder()
            .salesOrder(order)
            .product(product)
            .warehouse(warehouse)
            .quantity(qty)
            .qtyReserved(qty)
            .qtyBackordered(0)
            .unitPrice(unitPrice)
            .build();
    }


    public SalesOrderLine buildBackorderLine(SalesOrder order, Product product, WareHouse warehouse,
                                             long qty, BigDecimal unitPrice) {
        return SalesOrderLine.builder()
            .salesOrder(order)
            .product(product)
            .warehouse(warehouse)
            .quantity(qty)
            .qtyReserved(0)
            .qtyBackordered(qty)
            .unitPrice(unitPrice)
            .build();
    }


    public WareHouse findFallbackWarehouse(List<Inventory> inventories) {
        if (!inventories.isEmpty()) {
            return inventories.get(0).getWarehouse();
        }
        
        List<WareHouse> allWarehouses = wareHouseRepository.findAll();
        if (!allWarehouses.isEmpty()) {
            return allWarehouses.get(0);
        }
        
        throw new ResourceNotFoundException("Aucun warehouse disponible dans le système");
    }


    public void saveInventoriesInBatch(List<Inventory> inventoriesToUpdate) {
        if (!inventoriesToUpdate.isEmpty()) {
            inventoryRepository.saveAll(inventoriesToUpdate);
        }
    }
}
