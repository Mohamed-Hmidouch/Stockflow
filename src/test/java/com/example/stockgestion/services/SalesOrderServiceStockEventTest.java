package com.example.stockgestion.services;

import com.example.stockgestion.events.StockReceivedEvent;
import com.example.stockgestion.models.*;
import com.example.stockgestion.models.enums.SOStatus;
import com.example.stockgestion.repositories.*;
import com.example.stockgestion.services.helpers.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalesOrderServiceStockEventTest {

    @Mock
    private SalesOrderRepository salesOrderRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private SalesOrderLineRepository salesOrderLineRepository;

    @Mock
    private InventoryMovmentRepository inventoryMovmentRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private ClientValidator clientValidator;

    @Mock
    private ProductValidator productValidator;

    @Mock
    private StockReservationHelper stockReservationHelper;

    @Mock
    private OrderStatusHelper orderStatusHelper;

    @Mock
    private SalesOrderBuilder salesOrderBuilder;

    @InjectMocks
    private SalesOrderService salesOrderService;

    private UUID productId;
    private UUID warehouseId;
    private Product product;
    private WareHouse warehouse;
    private Inventory inventory;
    private SalesOrderLine backorderedLine;
    private SalesOrder salesOrder;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        warehouseId = UUID.randomUUID();

        product = new Product();
        product.setId(productId);

        warehouse = new WareHouse();
        warehouse.setId(warehouseId);

        inventory = new Inventory();
        inventory.setId(UUID.randomUUID());
        inventory.setProduct(product);
        inventory.setWarehouse(warehouse);
        inventory.setQtyOnHand(100);
        inventory.setQtyReserved(20);

        salesOrder = new SalesOrder();
        salesOrder.setId(UUID.randomUUID());
        salesOrder.setStatus(SOStatus.PARTIALLY_RESERVED);

        backorderedLine = new SalesOrderLine();
        backorderedLine.setId(UUID.randomUUID());
        backorderedLine.setProduct(product);
        backorderedLine.setQtyBackordered(50);
        backorderedLine.setQtyReserved(10);
        backorderedLine.setSalesOrder(salesOrder);
    }

    @Test
    void handleStockReceived_WhenNoInventory_ShouldReturnEarly() {
        // Given
        StockReceivedEvent event = new StockReceivedEvent(productId, warehouseId, 100);
        when(inventoryRepository.findByProduct_IdAndWarehouse_Id(productId, warehouseId))
                .thenReturn(Collections.emptyList());

        // When
        salesOrderService.handleStockReceived(event);

        // Then
        verify(salesOrderLineRepository, never()).findByProductIdAndQtyBackorderedGreaterThanOrderByCreatedAtAsc(any(), anyInt());
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void handleStockReceived_WhenNoAvailableStock_ShouldReturnEarly() {
        // Given
        inventory.setQtyOnHand(50);
        inventory.setQtyReserved(50); // Pas de stock disponible
        
        StockReceivedEvent event = new StockReceivedEvent(productId, warehouseId, 100);
        when(inventoryRepository.findByProduct_IdAndWarehouse_Id(productId, warehouseId))
                .thenReturn(Arrays.asList(inventory));

        // When
        salesOrderService.handleStockReceived(event);

        // Then
        verify(salesOrderLineRepository, never()).findByProductIdAndQtyBackorderedGreaterThanOrderByCreatedAtAsc(any(), anyInt());
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    void handleStockReceived_WhenNoBackorders_ShouldNotUpdateLines() {
        // Given
        StockReceivedEvent event = new StockReceivedEvent(productId, warehouseId, 100);
        when(inventoryRepository.findByProduct_IdAndWarehouse_Id(productId, warehouseId))
                .thenReturn(Arrays.asList(inventory));
        when(salesOrderLineRepository.findByProductIdAndQtyBackorderedGreaterThanOrderByCreatedAtAsc(productId, 0))
                .thenReturn(Collections.emptyList());

        // When
        salesOrderService.handleStockReceived(event);

        // Then
        verify(salesOrderLineRepository, never()).saveAll(any());
        verify(salesOrderRepository, never()).saveAll(any());
    }

    @Test
    void handleStockReceived_WhenBackordersExist_ShouldAllocateStock() {
        // Given
        salesOrder.setLines(Arrays.asList(backorderedLine));
        
        StockReceivedEvent event = new StockReceivedEvent(productId, warehouseId, 100);
        when(inventoryRepository.findByProduct_IdAndWarehouse_Id(productId, warehouseId))
                .thenReturn(Arrays.asList(inventory));
        when(salesOrderLineRepository.findByProductIdAndQtyBackorderedGreaterThanOrderByCreatedAtAsc(productId, 0))
                .thenReturn(Arrays.asList(backorderedLine));

        // When
        salesOrderService.handleStockReceived(event);

        // Then
        verify(salesOrderLineRepository).saveAll(anyList());
        verify(inventoryRepository).save(inventory);
    }

    @Test
    void handleStockReceived_WhenPartialAllocation_ShouldUpdateCorrectly() {
        // Given
        inventory.setQtyOnHand(100);
        inventory.setQtyReserved(90); // Seulement 10 disponibles
        backorderedLine.setQtyBackordered(50);
        salesOrder.setLines(Arrays.asList(backorderedLine));
        
        StockReceivedEvent event = new StockReceivedEvent(productId, warehouseId, 100);
        when(inventoryRepository.findByProduct_IdAndWarehouse_Id(productId, warehouseId))
                .thenReturn(Arrays.asList(inventory));
        when(salesOrderLineRepository.findByProductIdAndQtyBackorderedGreaterThanOrderByCreatedAtAsc(productId, 0))
                .thenReturn(Arrays.asList(backorderedLine));

        // When
        salesOrderService.handleStockReceived(event);

        // Then
        verify(inventoryRepository).save(any(Inventory.class));
        verify(salesOrderLineRepository).saveAll(anyList());
    }

    @Test
    void handleStockReceived_WhenOrderFullyFulfilled_ShouldChangeStatus() {
        // Given
        backorderedLine.setQtyBackordered(10); // Petit backorder
        inventory.setQtyOnHand(100);
        inventory.setQtyReserved(0); // Beaucoup de stock disponible
        salesOrder.setLines(Arrays.asList(backorderedLine));
        
        StockReceivedEvent event = new StockReceivedEvent(productId, warehouseId, 100);
        when(inventoryRepository.findByProduct_IdAndWarehouse_Id(productId, warehouseId))
                .thenReturn(Arrays.asList(inventory));
        when(salesOrderLineRepository.findByProductIdAndQtyBackorderedGreaterThanOrderByCreatedAtAsc(productId, 0))
                .thenReturn(Arrays.asList(backorderedLine));

        // When
        salesOrderService.handleStockReceived(event);

        // Then
        verify(salesOrderRepository).saveAll(anyList());
        verify(salesOrderLineRepository).saveAll(anyList());
        verify(inventoryRepository).save(any(Inventory.class));
    }
}
