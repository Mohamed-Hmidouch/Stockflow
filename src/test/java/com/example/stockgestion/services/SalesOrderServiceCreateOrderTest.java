package com.example.stockgestion.services;

import com.example.stockgestion.Dto.request.SalesOrderLineRequestDto;
import com.example.stockgestion.Dto.request.SalesOrderRequestDto;
import com.example.stockgestion.Dto.response.SalesOrderResponseDto;
import com.example.stockgestion.models.*;
import com.example.stockgestion.models.enums.SOStatus;
import com.example.stockgestion.repositories.InventoryRepository;
import com.example.stockgestion.repositories.SalesOrderRepository;
import com.example.stockgestion.repositories.SalesOrderLineRepository;
import com.example.stockgestion.services.helpers.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalesOrderServiceCreateOrderTest {

    @Mock
    private SalesOrderRepository salesOrderRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private SalesOrderLineRepository salesOrderLineRepository;

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

    private UUID clientId;
    private UUID productId;
    private UUID warehouseId;
    private Client client;
    private Product product;
    private WareHouse warehouse;
    private SalesOrder salesOrder;
    private SalesOrderRequestDto requestDto;

    @BeforeEach
    void setUp() {
        clientId = UUID.randomUUID();
        productId = UUID.randomUUID();
        warehouseId = UUID.randomUUID();

        // Setup Client
        client = new Client();
        client.setId(clientId);
        client.setName("Test Client");

        // Setup Product
        product = new Product();
        product.setId(productId);

        // Setup Warehouse
        warehouse = new WareHouse();
        warehouse.setId(warehouseId);

        // Setup SalesOrder
        salesOrder = new SalesOrder();
        salesOrder.setId(UUID.randomUUID());
        salesOrder.setClient(client);

        // Setup Request DTO
        SalesOrderLineRequestDto lineDto = new SalesOrderLineRequestDto();
        lineDto.setProductId(productId);
        lineDto.setQuantity(100L); // Demander 100 unités

        requestDto = new SalesOrderRequestDto();
        requestDto.setClientId(clientId);
        requestDto.setLines(List.of(lineDto));
    }

    @Test
    void createSalesOrder_WithPartialStock_ShouldSetPartiallyReservedStatus() {
        // Given: Stock partiel (50 disponibles sur 100 demandées)
        Inventory inventory = new Inventory();
        inventory.setId(UUID.randomUUID());
        inventory.setProduct(product);
        inventory.setWarehouse(warehouse);
        inventory.setQtyOnHand(50); // Seulement 50 en stock
        inventory.setQtyReserved(0);
        
        List<Inventory> inventories = List.of(inventory);
        
        // Setup mocks pour la validation
        when(clientValidator.validateAndGet(clientId)).thenReturn(client);
        when(salesOrderBuilder.initialize(client)).thenReturn(salesOrder);
        when(productValidator.validateAndGet(productId)).thenReturn(product);
        when(productValidator.validateAndGetPrice(product)).thenReturn(new BigDecimal("10.00"));
        when(stockReservationHelper.getInventoriesSortedByAvailability(product)).thenReturn(inventories);

        // Setup ReservationResult: 50 réservées, 50 en backorder
        SalesOrderLine reservedLine = new SalesOrderLine();
        reservedLine.setId(UUID.randomUUID());
        reservedLine.setSalesOrder(salesOrder);
        reservedLine.setProduct(product);
        reservedLine.setWarehouse(warehouse);
        reservedLine.setQuantity(50L);
        reservedLine.setQtyReserved(50L);
        reservedLine.setQtyBackordered(0L);
        reservedLine.setUnitPrice(new BigDecimal("10.00"));

        SalesOrderLine backorderLine = new SalesOrderLine();
        backorderLine.setId(UUID.randomUUID());
        backorderLine.setSalesOrder(salesOrder);
        backorderLine.setProduct(product);
        backorderLine.setWarehouse(warehouse);
        backorderLine.setQuantity(50L);
        backorderLine.setQtyReserved(0L);
        backorderLine.setQtyBackordered(50L); // 50 en backorder
        backorderLine.setUnitPrice(new BigDecimal("10.00"));

        ReservationResult reservationResult = new ReservationResult(
                List.of(reservedLine, backorderLine),
                true,  // hasReserved
                true   // hasBackorder
        );

        when(stockReservationHelper.reserveAcrossWarehouses(
                eq(salesOrder), eq(product), any(BigDecimal.class), eq(100L), 
                eq(inventories), anyList())).thenReturn(reservationResult);

        // Le status doit être PARTIALLY_RESERVED
        when(orderStatusHelper.determineStatus(true, true)).thenReturn(SOStatus.PARTIALLY_RESERVED);

        // Setup pour la sauvegarde
        SalesOrder savedOrder = new SalesOrder();
        savedOrder.setId(UUID.randomUUID());
        savedOrder.setStatus(SOStatus.PARTIALLY_RESERVED);
        savedOrder.setLines(List.of(reservedLine, backorderLine));
        
        when(salesOrderRepository.save(any(SalesOrder.class))).thenReturn(savedOrder);
        
        SalesOrderResponseDto responseDto = new SalesOrderResponseDto();
        responseDto.setId(savedOrder.getId());
        responseDto.setStatus(SOStatus.PARTIALLY_RESERVED);
        when(modelMapper.map(savedOrder, SalesOrderResponseDto.class)).thenReturn(responseDto);

        // When
        SalesOrderResponseDto result = salesOrderService.createSalesOrder(requestDto);

        // Then
        assertNotNull(result);
        assertEquals(SOStatus.PARTIALLY_RESERVED, result.getStatus());

        // Vérifier que le helper de détermination de statut a été appelé avec les bons paramètres
        verify(orderStatusHelper).determineStatus(true, true); // hasBackorder=true, hasReserved=true

        // Vérifier la construction de la commande
        verify(salesOrderBuilder).initialize(client);
        verify(salesOrderBuilder).finalize(eq(salesOrder), anyList(), any(BigDecimal.class), eq(SOStatus.PARTIALLY_RESERVED));
        
        // Vérifier la sauvegarde des inventaires
        verify(stockReservationHelper).saveInventoriesInBatch(anyList());
        
        // Vérifier la sauvegarde de la commande
        verify(salesOrderRepository).save(salesOrder);
    }

    @Test
    void createSalesOrder_WithPartialStock_ShouldCalculateBackorderedQuantityCorrectly() {
        // Given: Stock partiel (30 disponibles sur 100 demandées)
        Inventory inventory = new Inventory();
        inventory.setId(UUID.randomUUID());
        inventory.setProduct(product);
        inventory.setWarehouse(warehouse);
        inventory.setQtyOnHand(30); // Seulement 30 en stock
        inventory.setQtyReserved(0);
        
        List<Inventory> inventories = List.of(inventory);
        
        // Setup mocks pour la validation
        when(clientValidator.validateAndGet(clientId)).thenReturn(client);
        when(salesOrderBuilder.initialize(client)).thenReturn(salesOrder);
        when(productValidator.validateAndGet(productId)).thenReturn(product);
        when(productValidator.validateAndGetPrice(product)).thenReturn(new BigDecimal("15.00"));
        when(stockReservationHelper.getInventoriesSortedByAvailability(product)).thenReturn(inventories);

        // Setup ReservationResult: 30 réservées, 70 en backorder
        SalesOrderLine reservedLine = new SalesOrderLine();
        reservedLine.setQuantity(30L);
        reservedLine.setQtyReserved(30L);
        reservedLine.setQtyBackordered(0L);

        SalesOrderLine backorderLine = new SalesOrderLine();
        backorderLine.setQuantity(70L);
        backorderLine.setQtyReserved(0L);
        backorderLine.setQtyBackordered(70L); // 70 en backorder

        ReservationResult reservationResult = new ReservationResult(
                List.of(reservedLine, backorderLine),
                true,   // hasReserved
                true    // hasBackorder
        );

        when(stockReservationHelper.reserveAcrossWarehouses(
                eq(salesOrder), eq(product), any(BigDecimal.class), eq(100L), 
                eq(inventories), anyList())).thenReturn(reservationResult);

        when(orderStatusHelper.determineStatus(true, true)).thenReturn(SOStatus.PARTIALLY_RESERVED);

        // Capturer les lignes pour vérifier les quantités
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<SalesOrderLine>> linesCaptor = ArgumentCaptor.forClass(List.class);
        
        SalesOrder savedOrder = new SalesOrder();
        when(salesOrderRepository.save(any(SalesOrder.class))).thenReturn(savedOrder);
        when(modelMapper.map(savedOrder, SalesOrderResponseDto.class)).thenReturn(new SalesOrderResponseDto());

        // When
        salesOrderService.createSalesOrder(requestDto);

        // Then
        verify(salesOrderBuilder).finalize(eq(salesOrder), linesCaptor.capture(), any(BigDecimal.class), eq(SOStatus.PARTIALLY_RESERVED));
        
        List<SalesOrderLine> capturedLines = linesCaptor.getValue();
        assertEquals(2, capturedLines.size());

        // Vérifier que les quantités sont correctes
        long totalOrdered = capturedLines.stream().mapToLong(SalesOrderLine::getQuantity).sum();
        long totalReserved = capturedLines.stream().mapToLong(SalesOrderLine::getQtyReserved).sum();
        long totalBackordered = capturedLines.stream().mapToLong(SalesOrderLine::getQtyBackordered).sum();

        assertEquals(100L, totalOrdered, "Total commandé doit être 100");
        assertEquals(30L, totalReserved, "Total réservé doit être 30");
        assertEquals(70L, totalBackordered, "Total en backorder doit être 70");
    }

    @Test
    void createSalesOrder_WithNoStock_ShouldSetBackorderedStatus() {
        // Given: Aucun stock disponible
        Inventory inventory = new Inventory();
        inventory.setQtyOnHand(0); // Aucun stock
        inventory.setQtyReserved(0);
        
        List<Inventory> inventories = List.of(inventory);
        
        when(clientValidator.validateAndGet(clientId)).thenReturn(client);
        when(salesOrderBuilder.initialize(client)).thenReturn(salesOrder);
        when(productValidator.validateAndGet(productId)).thenReturn(product);
        when(productValidator.validateAndGetPrice(product)).thenReturn(new BigDecimal("10.00"));
        when(stockReservationHelper.getInventoriesSortedByAvailability(product)).thenReturn(inventories);

        // Tout en backorder
        SalesOrderLine backorderLine = new SalesOrderLine();
        backorderLine.setQuantity(100L);
        backorderLine.setQtyReserved(0L);
        backorderLine.setQtyBackordered(100L);

        ReservationResult reservationResult = new ReservationResult(
                List.of(backorderLine),
                false,  // hasReserved - Rien de réservé
                true    // hasBackorder - Tout en backorder
        );

        when(stockReservationHelper.reserveAcrossWarehouses(
                any(), any(), any(), anyLong(), any(), anyList())).thenReturn(reservationResult);

        when(orderStatusHelper.determineStatus(true, false)).thenReturn(SOStatus.BACKORDERED);

        SalesOrder savedOrder = new SalesOrder();
        when(salesOrderRepository.save(any())).thenReturn(savedOrder);
        
        SalesOrderResponseDto responseDto = new SalesOrderResponseDto();
        responseDto.setStatus(SOStatus.BACKORDERED);
        when(modelMapper.map(savedOrder, SalesOrderResponseDto.class)).thenReturn(responseDto);

        // When
        SalesOrderResponseDto result = salesOrderService.createSalesOrder(requestDto);

        // Then
        assertEquals(SOStatus.BACKORDERED, result.getStatus());
        verify(orderStatusHelper).determineStatus(true, false); // hasBackorder=true, hasReserved=false
    }

    @Test
    void createSalesOrder_WithFullStock_ShouldSetReservedStatus() {
        // Given: Stock complet disponible
        Inventory inventory = new Inventory();
        inventory.setQtyOnHand(150); // Plus que suffisant
        inventory.setQtyReserved(0);
        
        List<Inventory> inventories = List.of(inventory);
        
        when(clientValidator.validateAndGet(clientId)).thenReturn(client);
        when(salesOrderBuilder.initialize(client)).thenReturn(salesOrder);
        when(productValidator.validateAndGet(productId)).thenReturn(product);
        when(productValidator.validateAndGetPrice(product)).thenReturn(new BigDecimal("10.00"));
        when(stockReservationHelper.getInventoriesSortedByAvailability(product)).thenReturn(inventories);

        // Tout réservé
        SalesOrderLine reservedLine = new SalesOrderLine();
        reservedLine.setQuantity(100L);
        reservedLine.setQtyReserved(100L);
        reservedLine.setQtyBackordered(0L);

        ReservationResult reservationResult = new ReservationResult(
                List.of(reservedLine),
                true,   // hasReserved - Tout réservé
                false   // hasBackorder - Rien en backorder
        );

        when(stockReservationHelper.reserveAcrossWarehouses(
                any(), any(), any(), anyLong(), any(), anyList())).thenReturn(reservationResult);

        when(orderStatusHelper.determineStatus(false, true)).thenReturn(SOStatus.RESERVED);

        SalesOrder savedOrder = new SalesOrder();
        when(salesOrderRepository.save(any())).thenReturn(savedOrder);
        
        SalesOrderResponseDto responseDto = new SalesOrderResponseDto();
        responseDto.setStatus(SOStatus.RESERVED);
        when(modelMapper.map(savedOrder, SalesOrderResponseDto.class)).thenReturn(responseDto);

        // When
        SalesOrderResponseDto result = salesOrderService.createSalesOrder(requestDto);

        // Then
        assertEquals(SOStatus.RESERVED, result.getStatus());
        verify(orderStatusHelper).determineStatus(false, true); // hasBackorder=false, hasReserved=true
    }
}