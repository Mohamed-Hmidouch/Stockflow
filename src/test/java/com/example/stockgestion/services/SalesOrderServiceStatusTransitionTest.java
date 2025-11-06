package com.example.stockgestion.services;

import com.example.stockgestion.Dto.response.SalesOrderResponseDto;
import com.example.stockgestion.exception.BusinessRuleException;
import com.example.stockgestion.exception.ResourceNotFoundException;
import com.example.stockgestion.models.*;
import com.example.stockgestion.models.enums.MovementType;
import com.example.stockgestion.models.enums.SOStatus;
import com.example.stockgestion.repositories.InventoryMovmentRepository;
import com.example.stockgestion.repositories.InventoryRepository;
import com.example.stockgestion.repositories.SalesOrderRepository;

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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalesOrderServiceStatusTransitionTest {

    @Mock
    private SalesOrderRepository salesOrderRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryMovmentRepository inventoryMovmentRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private SalesOrderService salesOrderService;

    private UUID orderId;
    private UUID productId;
    private UUID warehouseId;
    private UUID clientId;
    private SalesOrder salesOrder;
    private SalesOrderLine orderLine;
    private Product product;
    private WareHouse warehouse;
    private Client client;
    private Inventory inventory;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        productId = UUID.randomUUID();
        warehouseId = UUID.randomUUID();
        clientId = UUID.randomUUID();

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

        // Setup Inventory
        inventory = new Inventory();
        inventory.setId(UUID.randomUUID());
        inventory.setProduct(product);
        inventory.setWarehouse(warehouse);
        inventory.setQtyOnHand(100);
        inventory.setQtyReserved(50);

        // Setup SalesOrderLine
        orderLine = new SalesOrderLine();
        orderLine.setId(UUID.randomUUID());
        orderLine.setProduct(product);
        orderLine.setWarehouse(warehouse);
        orderLine.setQuantity(50L);
        orderLine.setQtyReserved(50L);
        orderLine.setQtyBackordered(0L);
        orderLine.setUnitPrice(new BigDecimal("10.00"));

        // Setup SalesOrder
        salesOrder = new SalesOrder();
        salesOrder.setId(orderId);
        salesOrder.setClient(client);
        salesOrder.setLines(List.of(orderLine));
        orderLine.setSalesOrder(salesOrder); // Bi-directional relationship
    }

    @Test
    void shipOrder_WithReservedStatus_ShouldTransitionToDelivered() {
        // Given: Commande avec statut RESERVED
        salesOrder.setStatus(SOStatus.RESERVED);
        
        when(salesOrderRepository.findById(orderId)).thenReturn(Optional.of(salesOrder));
        when(inventoryRepository.findByProduct_IdAndWarehouse_Id(productId, warehouseId))
                .thenReturn(List.of(inventory));

        // Captured objects for verification
        ArgumentCaptor<SalesOrder> orderCaptor = ArgumentCaptor.forClass(SalesOrder.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Inventory>> inventoryCaptor = ArgumentCaptor.forClass(List.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<InventoryMovement>> movementCaptor = ArgumentCaptor.forClass(List.class);

        SalesOrder savedOrder = new SalesOrder();
        savedOrder.setId(orderId);
        savedOrder.setStatus(SOStatus.DELIVERED);
        when(salesOrderRepository.save(any(SalesOrder.class))).thenReturn(savedOrder);

        SalesOrderResponseDto responseDto = new SalesOrderResponseDto();
        responseDto.setId(orderId);
        responseDto.setStatus(SOStatus.DELIVERED);
        when(modelMapper.map(savedOrder, SalesOrderResponseDto.class)).thenReturn(responseDto);

        // When
        SalesOrderResponseDto result = salesOrderService.shipOrder(orderId);

        // Then
        assertNotNull(result);
        assertEquals(SOStatus.DELIVERED, result.getStatus());

        // Vérifier que le statut de la commande a bien été changé
        verify(salesOrderRepository).save(orderCaptor.capture());
        SalesOrder capturedOrder = orderCaptor.getValue();
        assertEquals(SOStatus.DELIVERED, capturedOrder.getStatus());

        // Vérifier que l'inventaire a été mis à jour
        verify(inventoryRepository).saveAll(inventoryCaptor.capture());
        List<Inventory> capturedInventories = inventoryCaptor.getValue();
        assertEquals(1, capturedInventories.size());
        
        Inventory updatedInventory = capturedInventories.get(0);
        assertEquals(50, updatedInventory.getQtyOnHand()); // 100 - 50 = 50
        assertEquals(0, updatedInventory.getQtyReserved());  // 50 - 50 = 0

        // Vérifier qu'un mouvement d'inventaire a été créé
        verify(inventoryMovmentRepository).saveAll(movementCaptor.capture());
        List<InventoryMovement> capturedMovements = movementCaptor.getValue();
        assertEquals(1, capturedMovements.size());
        
        InventoryMovement movement = capturedMovements.get(0);
        assertEquals(-50, movement.getQuantity()); // Sortie de stock
        assertEquals(MovementType.OUTBOUND, movement.getType());
        assertEquals(product, movement.getProduct());
        assertEquals(warehouse, movement.getWarehouse());
        assertNotNull(movement.getOccurredAt());
    }

    @Test
    void shipOrder_WithPartiallyReservedStatus_ShouldTransitionToDelivered() {
        // Given: Commande avec statut PARTIALLY_RESERVED
        salesOrder.setStatus(SOStatus.PARTIALLY_RESERVED);
        
        when(salesOrderRepository.findById(orderId)).thenReturn(Optional.of(salesOrder));
        when(inventoryRepository.findByProduct_IdAndWarehouse_Id(productId, warehouseId))
                .thenReturn(List.of(inventory));

        ArgumentCaptor<SalesOrder> orderCaptor = ArgumentCaptor.forClass(SalesOrder.class);
        
        SalesOrder savedOrder = new SalesOrder();
        savedOrder.setStatus(SOStatus.DELIVERED);
        when(salesOrderRepository.save(any(SalesOrder.class))).thenReturn(savedOrder);

        SalesOrderResponseDto responseDto = new SalesOrderResponseDto();
        responseDto.setStatus(SOStatus.DELIVERED);
        when(modelMapper.map(savedOrder, SalesOrderResponseDto.class)).thenReturn(responseDto);

        // When
        SalesOrderResponseDto result = salesOrderService.shipOrder(orderId);

        // Then
        assertEquals(SOStatus.DELIVERED, result.getStatus());
        
        // Vérifier la transition de statut
        verify(salesOrderRepository).save(orderCaptor.capture());
        assertEquals(SOStatus.DELIVERED, orderCaptor.getValue().getStatus());
    }

    @Test
    void shipOrder_WithInvalidStatus_ShouldThrowBusinessRuleException() {
        // Given: Commande avec statut CREATED (non expédiable)
        salesOrder.setStatus(SOStatus.CREATED);
        
        when(salesOrderRepository.findById(orderId)).thenReturn(Optional.of(salesOrder));

        // When & Then
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, 
                () -> salesOrderService.shipOrder(orderId));
        
        assertEquals("Impossible d'epédier une commande qui n'est pas reserver !!!!", exception.getMessage());
        
        // Vérifier qu'aucune sauvegarde n'a eu lieu
        verify(salesOrderRepository, never()).save(any());
        verify(inventoryRepository, never()).saveAll(any());
        verify(inventoryMovmentRepository, never()).saveAll(any());
    }

    @Test
    void shipOrder_WithDeliveredStatus_ShouldThrowBusinessRuleException() {
        // Given: Commande déjà livrée
        salesOrder.setStatus(SOStatus.DELIVERED);
        
        when(salesOrderRepository.findById(orderId)).thenReturn(Optional.of(salesOrder));

        // When & Then
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, 
                () -> salesOrderService.shipOrder(orderId));
        
        assertEquals("Impossible d'epédier une commande qui n'est pas reserver !!!!", exception.getMessage());
    }

    @Test
    void cancelOrder_WithReservedStatus_ShouldTransitionToCanceled() {
        // Given: Commande avec statut RESERVED
        salesOrder.setStatus(SOStatus.RESERVED);
        
        when(salesOrderRepository.findById(orderId)).thenReturn(Optional.of(salesOrder));
        when(inventoryRepository.findByProduct_IdAndWarehouse_Id(productId, warehouseId))
                .thenReturn(List.of(inventory));

        ArgumentCaptor<SalesOrder> orderCaptor = ArgumentCaptor.forClass(SalesOrder.class);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Inventory>> inventoryCaptor = ArgumentCaptor.forClass(List.class);

        SalesOrder savedOrder = new SalesOrder();
        savedOrder.setId(orderId);
        savedOrder.setStatus(SOStatus.CANCELED);
        when(salesOrderRepository.save(any(SalesOrder.class))).thenReturn(savedOrder);

        SalesOrderResponseDto responseDto = new SalesOrderResponseDto();
        responseDto.setId(orderId);
        responseDto.setStatus(SOStatus.CANCELED);
        when(modelMapper.map(savedOrder, SalesOrderResponseDto.class)).thenReturn(responseDto);

        // When
        SalesOrderResponseDto result = salesOrderService.cancelOrder(orderId);

        // Then
        assertNotNull(result);
        assertEquals(SOStatus.CANCELED, result.getStatus());

        // Vérifier la transition de statut
        verify(salesOrderRepository).save(orderCaptor.capture());
        SalesOrder capturedOrder = orderCaptor.getValue();
        assertEquals(SOStatus.CANCELED, capturedOrder.getStatus());

        // Vérifier que les quantités réservées ont été libérées
        verify(inventoryRepository).saveAll(inventoryCaptor.capture());
        List<Inventory> capturedInventories = inventoryCaptor.getValue();
        assertEquals(1, capturedInventories.size());
        
        Inventory updatedInventory = capturedInventories.get(0);
        assertEquals(0, updatedInventory.getQtyReserved()); // 50 - 50 = 0 (libération)
        assertEquals(100, updatedInventory.getQtyOnHand()); // Inchangé
    }

    @Test
    void cancelOrder_WithPartiallyReservedStatus_ShouldTransitionToCanceled() {
        // Given: Commande avec statut PARTIALLY_RESERVED
        salesOrder.setStatus(SOStatus.PARTIALLY_RESERVED);
        
        when(salesOrderRepository.findById(orderId)).thenReturn(Optional.of(salesOrder));
        when(inventoryRepository.findByProduct_IdAndWarehouse_Id(productId, warehouseId))
                .thenReturn(List.of(inventory));

        ArgumentCaptor<SalesOrder> orderCaptor = ArgumentCaptor.forClass(SalesOrder.class);
        
        SalesOrder savedOrder = new SalesOrder();
        savedOrder.setStatus(SOStatus.CANCELED);
        when(salesOrderRepository.save(any(SalesOrder.class))).thenReturn(savedOrder);

        SalesOrderResponseDto responseDto = new SalesOrderResponseDto();
        responseDto.setStatus(SOStatus.CANCELED);
        when(modelMapper.map(savedOrder, SalesOrderResponseDto.class)).thenReturn(responseDto);

        // When
        SalesOrderResponseDto result = salesOrderService.cancelOrder(orderId);

        // Then
        assertEquals(SOStatus.CANCELED, result.getStatus());
        
        // Vérifier la transition de statut
        verify(salesOrderRepository).save(orderCaptor.capture());
        assertEquals(SOStatus.CANCELED, orderCaptor.getValue().getStatus());
    }

    @Test
    void cancelOrder_WithBackorderedStatus_ShouldTransitionToCanceled() {
        // Given: Commande avec statut BACKORDERED (pas de quantités réservées)
        orderLine.setQtyReserved(0L);
        orderLine.setQtyBackordered(50L);
        salesOrder.setStatus(SOStatus.BACKORDERED);
        
        when(salesOrderRepository.findById(orderId)).thenReturn(Optional.of(salesOrder));

        ArgumentCaptor<SalesOrder> orderCaptor = ArgumentCaptor.forClass(SalesOrder.class);
        
        SalesOrder savedOrder = new SalesOrder();
        savedOrder.setStatus(SOStatus.CANCELED);
        when(salesOrderRepository.save(any(SalesOrder.class))).thenReturn(savedOrder);

        SalesOrderResponseDto responseDto = new SalesOrderResponseDto();
        responseDto.setStatus(SOStatus.CANCELED);
        when(modelMapper.map(savedOrder, SalesOrderResponseDto.class)).thenReturn(responseDto);

        // When
        SalesOrderResponseDto result = salesOrderService.cancelOrder(orderId);

        // Then
        assertEquals(SOStatus.CANCELED, result.getStatus());
        
        // Vérifier la transition de statut
        verify(salesOrderRepository).save(orderCaptor.capture());
        assertEquals(SOStatus.CANCELED, orderCaptor.getValue().getStatus());

        // Aucune mise à jour d'inventaire nécessaire car rien n'était réservé
        verify(inventoryRepository, never()).saveAll(any());
    }

    @Test
    void cancelOrder_WithDeliveredStatus_ShouldThrowBusinessRuleException() {
        // Given: Commande déjà livrée
        salesOrder.setStatus(SOStatus.DELIVERED);
        
        when(salesOrderRepository.findById(orderId)).thenReturn(Optional.of(salesOrder));

        // When & Then
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, 
                () -> salesOrderService.cancelOrder(orderId));
        
        assertEquals("Impossible d'annuler une commande déjà expédiée ou livrée.", exception.getMessage());
        
        // Vérifier qu'aucune modification n'a eu lieu
        verify(salesOrderRepository, never()).save(any());
        verify(inventoryRepository, never()).saveAll(any());
    }

    @Test
    void cancelOrder_WithAlreadyCanceledStatus_ShouldReturnSameOrder() {
        // Given: Commande déjà annulée
        salesOrder.setStatus(SOStatus.CANCELED);
        
        when(salesOrderRepository.findById(orderId)).thenReturn(Optional.of(salesOrder));

        SalesOrderResponseDto responseDto = new SalesOrderResponseDto();
        responseDto.setStatus(SOStatus.CANCELED);
        when(modelMapper.map(salesOrder, SalesOrderResponseDto.class)).thenReturn(responseDto);

        // When
        SalesOrderResponseDto result = salesOrderService.cancelOrder(orderId);

        // Then
        assertEquals(SOStatus.CANCELED, result.getStatus());
        
        // Vérifier qu'aucune sauvegarde supplémentaire n'a eu lieu
        verify(salesOrderRepository, never()).save(any());
        verify(inventoryRepository, never()).saveAll(any());
    }

    @Test
    void shipOrder_WithNonExistentOrder_ShouldThrowResourceNotFoundException() {
        // Given: Commande inexistante
        when(salesOrderRepository.findById(orderId)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, 
                () -> salesOrderService.shipOrder(orderId));
        
        assertTrue(exception.getMessage().contains("SalesOrder"));
        assertTrue(exception.getMessage().contains("id"));
    }

    @Test
    void cancelOrder_WithNonExistentOrder_ShouldThrowResourceNotFoundException() {
        // Given: Commande inexistante
        when(salesOrderRepository.findById(orderId)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, 
                () -> salesOrderService.cancelOrder(orderId));
        
        assertTrue(exception.getMessage().contains("SalesOrder"));
        assertTrue(exception.getMessage().contains("id"));
    }
}