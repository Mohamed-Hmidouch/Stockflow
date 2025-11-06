package com.example.stockgestion.services;

import com.example.stockgestion.Dto.response.SalesOrderResponseDto;
import com.example.stockgestion.exception.BusinessRuleException;
import com.example.stockgestion.exception.ResourceNotFoundException;
import com.example.stockgestion.models.*;
import com.example.stockgestion.models.enums.SOStatus;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalesOrderServiceCancelOrderTest {

    @Mock
    private SalesOrderRepository salesOrderRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private SalesOrderService salesOrderService;

    private UUID orderId;
    private SalesOrder salesOrder;
    private Product product1, product2;
    private WareHouse warehouse1, warehouse2;
    private Client client;
    private Inventory inventory1, inventory2;
    private SalesOrderLine salesOrderLine1, salesOrderLine2;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        
        // Setup Products
        product1 = new Product();
        product1.setId(UUID.randomUUID());
        product1.setName("Test Product 1");
        product1.setSku("TEST-001");
        product1.setActive(true);

        product2 = new Product();
        product2.setId(UUID.randomUUID());
        product2.setName("Test Product 2");
        product2.setSku("TEST-002");
        product2.setActive(true);

        // Setup Warehouses
        warehouse1 = new WareHouse();
        warehouse1.setId(UUID.randomUUID());
        warehouse1.setName("Test Warehouse 1");
        warehouse1.setCode("WH-TEST-1");

        warehouse2 = new WareHouse();
        warehouse2.setId(UUID.randomUUID());
        warehouse2.setName("Test Warehouse 2");
        warehouse2.setCode("WH-TEST-2");

        // Setup Client
        client = new Client();
        client.setId(UUID.randomUUID());
        client.setName("Test Client");

        // Setup Inventories
        inventory1 = new Inventory();
        inventory1.setId(UUID.randomUUID());
        inventory1.setProduct(product1);
        inventory1.setWarehouse(warehouse1);
        inventory1.setQtyOnHand(100);
        inventory1.setQtyReserved(30); // 20 réservé pour notre commande + 10 pour d'autres

        inventory2 = new Inventory();
        inventory2.setId(UUID.randomUUID());
        inventory2.setProduct(product2);
        inventory2.setWarehouse(warehouse2);
        inventory2.setQtyOnHand(50);
        inventory2.setQtyReserved(25); // 15 réservé pour notre commande + 10 pour d'autres

        // Setup Sales Order Lines
        salesOrderLine1 = new SalesOrderLine();
        salesOrderLine1.setId(UUID.randomUUID());
        salesOrderLine1.setProduct(product1);
        salesOrderLine1.setWarehouse(warehouse1);
        salesOrderLine1.setQuantity(25);
        salesOrderLine1.setQtyReserved(20); // Quantité réservée à libérer
        salesOrderLine1.setQtyBackordered(5);
        salesOrderLine1.setUnitPrice(BigDecimal.valueOf(100.00));

        salesOrderLine2 = new SalesOrderLine();
        salesOrderLine2.setId(UUID.randomUUID());
        salesOrderLine2.setProduct(product2);
        salesOrderLine2.setWarehouse(warehouse2);
        salesOrderLine2.setQuantity(20);
        salesOrderLine2.setQtyReserved(15); // Quantité réservée à libérer
        salesOrderLine2.setQtyBackordered(5);
        salesOrderLine2.setUnitPrice(BigDecimal.valueOf(50.00));

        // Setup Sales Order
        salesOrder = new SalesOrder();
        salesOrder.setId(orderId);
        salesOrder.setClient(client);
        salesOrder.setStatus(SOStatus.PARTIALLY_RESERVED);
        salesOrder.setLines(new ArrayList<>(Arrays.asList(salesOrderLine1, salesOrderLine2)));
        salesOrder.setTotalPrice(BigDecimal.valueOf(3500.00));
        
        // Établir les relations bidirectionnelles
        salesOrderLine1.setSalesOrder(salesOrder);
        salesOrderLine2.setSalesOrder(salesOrder);
    }

    @Test
    void cancelOrder_ShouldReduceQtyReserved_WhenOrderIsReserved() {
        // Given
        salesOrder.setStatus(SOStatus.RESERVED);
        
        when(salesOrderRepository.findById(orderId)).thenReturn(Optional.of(salesOrder));
        when(inventoryRepository.findByProduct_IdAndWarehouse_Id(product1.getId(), warehouse1.getId()))
                .thenReturn(Arrays.asList(inventory1));
        when(inventoryRepository.findByProduct_IdAndWarehouse_Id(product2.getId(), warehouse2.getId()))
                .thenReturn(Arrays.asList(inventory2));
        
        SalesOrderResponseDto expectedResponse = new SalesOrderResponseDto();
        expectedResponse.setId(orderId);
        expectedResponse.setStatus(SOStatus.CANCELED);
        when(modelMapper.map(any(SalesOrder.class), eq(SalesOrderResponseDto.class)))
                .thenReturn(expectedResponse);
        
        when(salesOrderRepository.save(any(SalesOrder.class))).thenReturn(salesOrder);

        // When
        SalesOrderResponseDto result = salesOrderService.cancelOrder(orderId);

        // Then
        assertNotNull(result);
        assertEquals(SOStatus.CANCELED, result.getStatus());
        
        // Vérifier que qtyReserved est correctement réduit
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Inventory>> inventoryCaptor = ArgumentCaptor.forClass(List.class);
        verify(inventoryRepository).saveAll(inventoryCaptor.capture());
        
        List<Inventory> savedInventories = inventoryCaptor.getValue();
        assertEquals(2, savedInventories.size());
        
        // Vérifier inventory1 : qtyReserved doit passer de 30 à 10 (30 - 20)
        Inventory savedInventory1 = savedInventories.stream()
                .filter(inv -> inv.getProduct().getId().equals(product1.getId()))
                .findFirst()
                .orElseThrow();
        assertEquals(100, savedInventory1.getQtyOnHand()); // Pas de changement
        assertEquals(10, savedInventory1.getQtyReserved()); // 30 - 20 = 10
        
        // Vérifier inventory2 : qtyReserved doit passer de 25 à 10 (25 - 15)
        Inventory savedInventory2 = savedInventories.stream()
                .filter(inv -> inv.getProduct().getId().equals(product2.getId()))
                .findFirst()
                .orElseThrow();
        assertEquals(50, savedInventory2.getQtyOnHand()); // Pas de changement
        assertEquals(10, savedInventory2.getQtyReserved()); // 25 - 15 = 10
        
        // Vérifier qu'aucune quantité réservée ne devient négative
        assertTrue(savedInventory1.getQtyReserved() >= 0, 
                  "QtyReserved ne doit jamais être négatif pour inventory1. Valeur: " + savedInventory1.getQtyReserved());
        assertTrue(savedInventory2.getQtyReserved() >= 0, 
                  "QtyReserved ne doit jamais être négatif pour inventory2. Valeur: " + savedInventory2.getQtyReserved());
        
        // Vérifier que le statut de la commande est mis à jour
        ArgumentCaptor<SalesOrder> orderCaptor = ArgumentCaptor.forClass(SalesOrder.class);
        verify(salesOrderRepository).save(orderCaptor.capture());
        
        SalesOrder savedOrder = orderCaptor.getValue();
        assertEquals(SOStatus.CANCELED, savedOrder.getStatus());
    }

    @Test
    void cancelOrder_ShouldReduceQtyReserved_WhenOrderIsPartiallyReserved() {
        // Given - Order is PARTIALLY_RESERVED
        salesOrder.setStatus(SOStatus.PARTIALLY_RESERVED);
        
        when(salesOrderRepository.findById(orderId)).thenReturn(Optional.of(salesOrder));
        when(inventoryRepository.findByProduct_IdAndWarehouse_Id(product1.getId(), warehouse1.getId()))
                .thenReturn(Arrays.asList(inventory1));
        when(inventoryRepository.findByProduct_IdAndWarehouse_Id(product2.getId(), warehouse2.getId()))
                .thenReturn(Arrays.asList(inventory2));
        
        when(salesOrderRepository.save(any(SalesOrder.class))).thenReturn(salesOrder);
        when(modelMapper.map(any(SalesOrder.class), eq(SalesOrderResponseDto.class)))
                .thenReturn(new SalesOrderResponseDto());

        // When
        salesOrderService.cancelOrder(orderId);

        // Then - Vérifier que qtyReserved est correctement réduit
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Inventory>> inventoryCaptor = ArgumentCaptor.forClass(List.class);
        verify(inventoryRepository).saveAll(inventoryCaptor.capture());
        
        List<Inventory> savedInventories = inventoryCaptor.getValue();
        
        for (Inventory savedInventory : savedInventories) {
            if (savedInventory.getProduct().getId().equals(product1.getId())) {
                assertEquals(10, savedInventory.getQtyReserved()); // 30 - 20 = 10
            } else if (savedInventory.getProduct().getId().equals(product2.getId())) {
                assertEquals(10, savedInventory.getQtyReserved()); // 25 - 15 = 10
            }
            
            // Vérifier qu'aucune quantité ne devient négative
            assertTrue(savedInventory.getQtyReserved() >= 0, 
                      "QtyReserved ne doit jamais être négatif");
        }
    }

    @Test
    void cancelOrder_ShouldNotReduceQtyReserved_WhenOrderIsCreated() {
        // Given - Order with CREATED status (no reservations made yet)
        salesOrder.setStatus(SOStatus.CREATED);
        
        when(salesOrderRepository.findById(orderId)).thenReturn(Optional.of(salesOrder));
        when(salesOrderRepository.save(any(SalesOrder.class))).thenReturn(salesOrder);
        when(modelMapper.map(any(SalesOrder.class), eq(SalesOrderResponseDto.class)))
                .thenReturn(new SalesOrderResponseDto());

        // When
        salesOrderService.cancelOrder(orderId);

        // Then - Aucune modification d'inventaire car aucune réservation
        verify(inventoryRepository, never()).saveAll(any());
        
        // Mais le statut change quand même
        verify(salesOrderRepository).save(any(SalesOrder.class));
    }

    @Test
    void cancelOrder_ShouldHandleLinesWithZeroReservation() {
        // Given - Une ligne sans réservation
        salesOrderLine1.setQtyReserved(0); // Pas de réservation à libérer
        salesOrderLine2.setQtyReserved(15); // Gardons la réservation sur la ligne 2
        
        when(salesOrderRepository.findById(orderId)).thenReturn(Optional.of(salesOrder));
        when(inventoryRepository.findByProduct_IdAndWarehouse_Id(product2.getId(), warehouse2.getId()))
                .thenReturn(Arrays.asList(inventory2));
        
        when(salesOrderRepository.save(any(SalesOrder.class))).thenReturn(salesOrder);
        when(modelMapper.map(any(SalesOrder.class), eq(SalesOrderResponseDto.class)))
                .thenReturn(new SalesOrderResponseDto());

        // When
        salesOrderService.cancelOrder(orderId);

        // Then - Seul inventory2 devrait être modifié
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Inventory>> inventoryCaptor = ArgumentCaptor.forClass(List.class);
        verify(inventoryRepository).saveAll(inventoryCaptor.capture());
        
        List<Inventory> savedInventories = inventoryCaptor.getValue();
        assertEquals(1, savedInventories.size()); // Seulement inventory2
        
        Inventory savedInventory = savedInventories.get(0);
        assertEquals(product2.getId(), savedInventory.getProduct().getId());
        assertEquals(10, savedInventory.getQtyReserved()); // 25 - 15 = 10
    }

    @Test
    void cancelOrder_ShouldFail_WhenOrderNotFound() {
        // Given
        when(salesOrderRepository.findById(orderId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, 
                    () -> salesOrderService.cancelOrder(orderId));
        
        // Vérifier qu'aucune modification n'est effectuée
        verify(inventoryRepository, never()).saveAll(any());
        verify(salesOrderRepository, never()).save(any());
    }

    @Test
    void cancelOrder_ShouldFail_WhenOrderIsDelivered() {
        // Given
        salesOrder.setStatus(SOStatus.DELIVERED);
        when(salesOrderRepository.findById(orderId)).thenReturn(Optional.of(salesOrder));

        // When & Then
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, 
                                                     () -> salesOrderService.cancelOrder(orderId));
        
        assertTrue(exception.getMessage().contains("Impossible d'annuler une commande déjà expédiée ou livrée"));
        
        // Vérifier qu'aucune modification n'est effectuée
        verify(inventoryRepository, never()).saveAll(any());
        verify(salesOrderRepository, never()).save(any());
    }

    @Test
    void cancelOrder_ShouldReturnExisting_WhenOrderIsAlreadyCanceled() {
        // Given
        salesOrder.setStatus(SOStatus.CANCELED);
        when(salesOrderRepository.findById(orderId)).thenReturn(Optional.of(salesOrder));
        
        SalesOrderResponseDto expectedResponse = new SalesOrderResponseDto();
        expectedResponse.setStatus(SOStatus.CANCELED);
        when(modelMapper.map(any(SalesOrder.class), eq(SalesOrderResponseDto.class)))
                .thenReturn(expectedResponse);

        // When
        SalesOrderResponseDto result = salesOrderService.cancelOrder(orderId);

        // Then
        assertNotNull(result);
        assertEquals(SOStatus.CANCELED, result.getStatus());
        
        // Vérifier qu'aucune modification d'inventaire n'est effectuée
        verify(inventoryRepository, never()).saveAll(any());
        verify(salesOrderRepository, never()).save(any());
    }

    @Test
    void cancelOrder_ShouldFail_WhenInventoryNotFound() {
        // Given
        when(salesOrderRepository.findById(orderId)).thenReturn(Optional.of(salesOrder));
        when(inventoryRepository.findByProduct_IdAndWarehouse_Id(product1.getId(), warehouse1.getId()))
                .thenReturn(Arrays.asList()); // Inventaire introuvable

        // When & Then
        assertThrows(ResourceNotFoundException.class, 
                    () -> salesOrderService.cancelOrder(orderId));
        
        // Vérifier qu'aucune sauvegarde n'est effectuée
        verify(salesOrderRepository, never()).save(any());
    }

    @Test
    void cancelOrder_ShouldPreventNegativeReservedQuantity() {
        // Given - Test edge case où la libération pourrait rendre qtyReserved négatif
        inventory1.setQtyReserved(15); // Moins que ce qui est réservé sur la ligne (20)
        salesOrderLine1.setQtyReserved(20); // Plus que ce qui est disponible dans l'inventaire
        
        when(salesOrderRepository.findById(orderId)).thenReturn(Optional.of(salesOrder));
        when(inventoryRepository.findByProduct_IdAndWarehouse_Id(product1.getId(), warehouse1.getId()))
                .thenReturn(Arrays.asList(inventory1));
        when(inventoryRepository.findByProduct_IdAndWarehouse_Id(product2.getId(), warehouse2.getId()))
                .thenReturn(Arrays.asList(inventory2));
        
        when(salesOrderRepository.save(any(SalesOrder.class))).thenReturn(salesOrder);
        when(modelMapper.map(any(SalesOrder.class), eq(SalesOrderResponseDto.class)))
                .thenReturn(new SalesOrderResponseDto());

        // When
        salesOrderService.cancelOrder(orderId);

        // Then
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Inventory>> inventoryCaptor = ArgumentCaptor.forClass(List.class);
        verify(inventoryRepository).saveAll(inventoryCaptor.capture());
        
        List<Inventory> savedInventories = inventoryCaptor.getValue();
        
        Inventory savedInventory1 = savedInventories.stream()
                .filter(inv -> inv.getProduct().getId().equals(product1.getId()))
                .findFirst()
                .orElseThrow();
        
        // ✅ APRÈS LE FIX : Le système devrait empêcher les valeurs négatives
        // Le fix défensif doit mettre qtyReserved à 0 au lieu de -5
        assertEquals(0, savedInventory1.getQtyReserved(), 
                    "Le fix défensif doit empêcher les quantités négatives en mettant à 0");
        
        // Vérification générale que toutes les quantités sont non-négatives
        assertTrue(savedInventory1.getQtyReserved() >= 0, "QtyReserved ne doit jamais être négatif");
    }
}