package com.example.stockgestion.services.helpers;

import com.example.stockgestion.exception.ResourceNotFoundException;
import com.example.stockgestion.models.*;
import com.example.stockgestion.repositories.InventoryRepository;
import com.example.stockgestion.repositories.WareHouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockReservationHelperTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private WareHouseRepository wareHouseRepository;

    @InjectMocks
    private StockReservationHelper stockReservationHelper;

    private Product product;
    private WareHouse warehouse1, warehouse2;
    private Inventory inventory1, inventory2;
    private SalesOrder salesOrder;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(UUID.randomUUID());
        product.setName("Test Product");

        warehouse1 = new WareHouse();
        warehouse1.setId(UUID.randomUUID());
        warehouse1.setName("Warehouse 1");

        warehouse2 = new WareHouse();
        warehouse2.setId(UUID.randomUUID());
        warehouse2.setName("Warehouse 2");

        inventory1 = new Inventory();
        inventory1.setProduct(product);
        inventory1.setWarehouse(warehouse1);
        inventory1.setQtyOnHand(100);
        inventory1.setQtyReserved(20);

        inventory2 = new Inventory();
        inventory2.setProduct(product);
        inventory2.setWarehouse(warehouse2);
        inventory2.setQtyOnHand(50);
        inventory2.setQtyReserved(10);

        salesOrder = new SalesOrder();
        salesOrder.setId(UUID.randomUUID());
    }

    @Test
    void getInventoriesSortedByAvailability_ShouldSortByAvailableQty() {
        // Given
        when(inventoryRepository.findByProduct_Id(product.getId()))
            .thenReturn(Arrays.asList(inventory2, inventory1)); // inventory2 has less available

        // When
        List<Inventory> result = stockReservationHelper.getInventoriesSortedByAvailability(product);

        // Then
        assertEquals(2, result.size());
        // inventory1 has 80 available (100-20), inventory2 has 40 available (50-10)
        assertEquals(inventory1, result.get(0)); // More available should be first
        verify(inventoryRepository).findByProduct_Id(product.getId());
    }

    @Test
    void calculateAvailableQty_ShouldReturnCorrectAmount() {
        // When
        long result = stockReservationHelper.calculateAvailableQty(inventory1);

        // Then
        assertEquals(80, result); // 100 - 20
    }

    @Test
    void calculateAvailableQty_ShouldHandleZeroReserved() {
        // Given
        inventory1.setQtyReserved(0);

        // When
        long result = stockReservationHelper.calculateAvailableQty(inventory1);

        // Then
        assertEquals(100, result);
    }

    @Test
    void reserveInventory_ShouldIncreaseQtyReserved() {
        // Given
        List<Inventory> inventoriesToUpdate = new ArrayList<>();
        long initialReserved = inventory1.getQtyReserved();

        // When
        stockReservationHelper.reserveInventory(inventory1, 30, inventoriesToUpdate);

        // Then
        assertEquals(initialReserved + 30, inventory1.getQtyReserved());
        assertTrue(inventoriesToUpdate.contains(inventory1));
    }

    @Test
    void buildReservedLine_ShouldCreateCorrectLine() {
        // When
        SalesOrderLine result = stockReservationHelper.buildReservedLine(
            salesOrder, product, warehouse1, 50, BigDecimal.valueOf(10.00)
        );

        // Then
        assertNotNull(result);
        assertEquals(salesOrder, result.getSalesOrder());
        assertEquals(product, result.getProduct());
        assertEquals(warehouse1, result.getWarehouse());
        assertEquals(50, result.getQuantity());
        assertEquals(50, result.getQtyReserved());
        assertEquals(0, result.getQtyBackordered());
        assertEquals(BigDecimal.valueOf(10.00), result.getUnitPrice());
    }

    @Test
    void buildBackorderLine_ShouldCreateCorrectLine() {
        // When
        SalesOrderLine result = stockReservationHelper.buildBackorderLine(
            salesOrder, product, warehouse1, 30, BigDecimal.valueOf(15.00)
        );

        // Then
        assertNotNull(result);
        assertEquals(salesOrder, result.getSalesOrder());
        assertEquals(product, result.getProduct());
        assertEquals(warehouse1, result.getWarehouse());
        assertEquals(30, result.getQuantity());
        assertEquals(0, result.getQtyReserved());
        assertEquals(30, result.getQtyBackordered());
        assertEquals(BigDecimal.valueOf(15.00), result.getUnitPrice());
    }

    @Test
    void findFallbackWarehouse_ShouldReturnFirstInventoryWarehouse_WhenInventoriesExist() {
        // Given
        List<Inventory> inventories = Arrays.asList(inventory1, inventory2);

        // When
        WareHouse result = stockReservationHelper.findFallbackWarehouse(inventories);

        // Then
        assertEquals(warehouse1, result);
    }

    @Test
    void findFallbackWarehouse_ShouldReturnFirstWarehouse_WhenNoInventories() {
        // Given
        List<Inventory> emptyInventories = new ArrayList<>();
        when(wareHouseRepository.findAll()).thenReturn(Arrays.asList(warehouse1, warehouse2));

        // When
        WareHouse result = stockReservationHelper.findFallbackWarehouse(emptyInventories);

        // Then
        assertEquals(warehouse1, result);
        verify(wareHouseRepository).findAll();
    }

    @Test
    void findFallbackWarehouse_ShouldThrowException_WhenNoWarehousesAvailable() {
        // Given
        List<Inventory> emptyInventories = new ArrayList<>();
        when(wareHouseRepository.findAll()).thenReturn(new ArrayList<>());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
            () -> stockReservationHelper.findFallbackWarehouse(emptyInventories)
        );
    }

    @Test
    void saveInventoriesInBatch_ShouldSaveWhenNotEmpty() {
        // Given
        List<Inventory> inventories = Arrays.asList(inventory1, inventory2);

        // When
        stockReservationHelper.saveInventoriesInBatch(inventories);

        // Then
        verify(inventoryRepository).saveAll(inventories);
    }

    @Test
    void saveInventoriesInBatch_ShouldNotSaveWhenEmpty() {
        // Given
        List<Inventory> emptyList = new ArrayList<>();

        // When
        stockReservationHelper.saveInventoriesInBatch(emptyList);

        // Then
        verify(inventoryRepository, never()).saveAll(any());
    }

    @Test
    void reserveAcrossWarehouses_ShouldReserveFromSingleWarehouse_WhenSufficientStock() {
        // Given
        List<Inventory> inventories = Arrays.asList(inventory1);
        List<Inventory> inventoriesToUpdate = new ArrayList<>();
        long requestedQty = 50;

        // When
        ReservationResult result = stockReservationHelper.reserveAcrossWarehouses(
            salesOrder, product, BigDecimal.valueOf(10.00), requestedQty, inventories, inventoriesToUpdate
        );

        // Then
        assertNotNull(result);
        assertTrue(result.isHasReserved());
        assertFalse(result.isHasBackorder());
        assertEquals(1, result.getLines().size());
        assertEquals(1, inventoriesToUpdate.size());
        
        SalesOrderLine line = result.getLines().get(0);
        assertEquals(50, line.getQtyReserved());
        assertEquals(0, line.getQtyBackordered());
    }

    @Test
    void reserveAcrossWarehouses_ShouldReserveFromMultipleWarehouses() {
        // Given
        List<Inventory> inventories = Arrays.asList(inventory1, inventory2);
        List<Inventory> inventoriesToUpdate = new ArrayList<>();
        long requestedQty = 100; // inventory1 can provide 80, inventory2 can provide 20

        // When
        ReservationResult result = stockReservationHelper.reserveAcrossWarehouses(
            salesOrder, product, BigDecimal.valueOf(10.00), requestedQty, inventories, inventoriesToUpdate
        );

        // Then
        assertTrue(result.isHasReserved());
        assertFalse(result.isHasBackorder());
        assertEquals(2, result.getLines().size());
        assertEquals(2, inventoriesToUpdate.size());
    }

    @Test
    void reserveAcrossWarehouses_ShouldCreateBackorder_WhenInsufficientStock() {
        // Given
        List<Inventory> inventories = Arrays.asList(inventory1, inventory2);
        List<Inventory> inventoriesToUpdate = new ArrayList<>();
        long requestedQty = 200; // Total available is 120 (80+40)

        // When
        ReservationResult result = stockReservationHelper.reserveAcrossWarehouses(
            salesOrder, product, BigDecimal.valueOf(10.00), requestedQty, inventories, inventoriesToUpdate
        );

        // Then
        assertTrue(result.isHasReserved());
        assertTrue(result.isHasBackorder());
        assertEquals(3, result.getLines().size()); // 2 reserved + 1 backorder
        
        // Last line should be backorder
        SalesOrderLine backorderLine = result.getLines().get(2);
        assertEquals(0, backorderLine.getQtyReserved());
        assertTrue(backorderLine.getQtyBackordered() > 0);
    }

    @Test
    void reserveAcrossWarehouses_ShouldSkipInventoryWithNoStock() {
        // Given
        inventory2.setQtyOnHand(10);
        inventory2.setQtyReserved(10); // No available stock
        
        List<Inventory> inventories = Arrays.asList(inventory1, inventory2);
        List<Inventory> inventoriesToUpdate = new ArrayList<>();

        // When
        ReservationResult result = stockReservationHelper.reserveAcrossWarehouses(
            salesOrder, product, BigDecimal.valueOf(10.00), 50, inventories, inventoriesToUpdate
        );

        // Then
        assertEquals(1, result.getLines().size()); // Only from inventory1
        assertEquals(1, inventoriesToUpdate.size()); // Only inventory1 updated
    }

    @Test
    void reserveAcrossWarehouses_ShouldCreateOnlyBackorder_WhenNoStockAvailable() {
        // Given
        inventory1.setQtyReserved(100); // All reserved
        inventory2.setQtyReserved(50); // All reserved
        
        List<Inventory> inventories = Arrays.asList(inventory1, inventory2);
        List<Inventory> inventoriesToUpdate = new ArrayList<>();

        // When
        ReservationResult result = stockReservationHelper.reserveAcrossWarehouses(
            salesOrder, product, BigDecimal.valueOf(10.00), 50, inventories, inventoriesToUpdate
        );

        // Then
        assertFalse(result.isHasReserved());
        assertTrue(result.isHasBackorder());
        assertEquals(1, result.getLines().size());
        assertEquals(0, inventoriesToUpdate.size());
        
        SalesOrderLine line = result.getLines().get(0);
        assertEquals(50, line.getQtyBackordered());
        assertEquals(0, line.getQtyReserved());
    }
}
