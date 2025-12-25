package com.example.stockgestion.services;

import com.example.stockgestion.Dto.request.ShipmentRequestDto;
import com.example.stockgestion.Dto.response.ShipmentResponseDto;
import com.example.stockgestion.exception.BusinessRuleException;
import com.example.stockgestion.exception.ResourceNotFoundException;
import com.example.stockgestion.models.*;
import com.example.stockgestion.models.enums.MovementType;
import com.example.stockgestion.models.enums.SOStatus;
import com.example.stockgestion.models.enums.ShipmentStatus;
import com.example.stockgestion.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests pour le service Shipment
 * US10 - Créer l'expédition
 * US11 - Marquer la commande SHIPPED puis DELIVERED
 */
@ExtendWith(MockitoExtension.class)
class ShipmentServiceTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private SalesOrderRepository salesOrderRepository;

    @Mock
    private CarrierRepository carrierRepository;

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryMovmentRepository inventoryMovementRepository;

    @InjectMocks
    private ShipmentService shipmentService;

    private SalesOrder salesOrder;
    private Carrier carrier;
    private Product product;
    private WareHouse warehouse;
    private Inventory inventory;
    private SalesOrderLine salesOrderLine;

    @BeforeEach
    void setUp() {
        // Setup Product
        product = new Product();
        product.setId(UUID.randomUUID());
        product.setSku("PROD-001");
        product.setName("Test Product");

        // Setup Warehouse
        warehouse = new WareHouse();
        warehouse.setId(UUID.randomUUID());
        warehouse.setCode("WH-01");
        warehouse.setName("Main Warehouse");

        // Setup Carrier
        carrier = new Carrier();
        carrier.setId(UUID.randomUUID());
        carrier.setName("DHL");
        carrier.setActive(true);

        // Setup Client
        Client client = new Client();
        client.setId(UUID.randomUUID());
        client.setName("Test Client");

        // Setup SalesOrderLine
        salesOrderLine = new SalesOrderLine();
        salesOrderLine.setId(UUID.randomUUID());
        salesOrderLine.setProduct(product);
        salesOrderLine.setWarehouse(warehouse);
        salesOrderLine.setQuantity(10);
        salesOrderLine.setQtyReserved(10);
        salesOrderLine.setQtyBackordered(0);

        // Setup SalesOrder
        salesOrder = SalesOrder.builder()
                .id(UUID.randomUUID())
                .client(client)
                .status(SOStatus.RESERVED)
                .createdAt(Instant.now())
                .lines(List.of(salesOrderLine))
                .build();

        salesOrderLine.setSalesOrder(salesOrder);

        // Setup Inventory
        inventory = Inventory.builder()
                .id(UUID.randomUUID())
                .product(product)
                .warehouse(warehouse)
                .qtyOnHand(100L)
                .qtyReserved(10L)
                .build();
    }

    @Test
    @DisplayName("US10 - Given commande RESERVED, When créer Shipment, Then status = PLANNED et commande reste RESERVED")
    void testCreateShipmentForReservedOrder() {
        // Given
        ShipmentRequestDto request = new ShipmentRequestDto();
        request.setSalesOrderId(salesOrder.getId());
        request.setCarrierId(carrier.getId());
        request.setTrackingNumber("DHL-12345");

        when(salesOrderRepository.findById(salesOrder.getId())).thenReturn(Optional.of(salesOrder));
        when(carrierRepository.findById(carrier.getId())).thenReturn(Optional.of(carrier));
        when(shipmentRepository.countByPlannedDepartureDateBetween(any(), any())).thenReturn(0L);
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> {
            Shipment s = invocation.getArgument(0);
            s.setId(UUID.randomUUID());
            return s;
        });

        // When
        ShipmentResponseDto result = shipmentService.createShipment(request);

        // Then
        assertNotNull(result);
        assertEquals(ShipmentStatus.PLANNED, result.getStatus());
        assertEquals(SOStatus.RESERVED, salesOrder.getStatus()); // La commande reste RESERVED
        assertNotNull(result.getPlannedDepartureDate());
        assertEquals("DHL-12345", result.getTrackingNumber());

        verify(shipmentRepository, times(1)).save(any(Shipment.class));
    }

    @Test
    @DisplayName("US10 - Given commande non RESERVED, When créer Shipment, Then BusinessRuleException")
    void testCreateShipmentForNonReservedOrder() {
        // Given
        salesOrder.setStatus(SOStatus.CREATED);
        ShipmentRequestDto request = new ShipmentRequestDto();
        request.setSalesOrderId(salesOrder.getId());
        request.setCarrierId(carrier.getId());

        when(salesOrderRepository.findById(salesOrder.getId())).thenReturn(Optional.of(salesOrder));

        // When & Then
        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> shipmentService.createShipment(request)
        );

        assertTrue(exception.getMessage().contains("RESERVED"));
    }

    @Test
    @DisplayName("US10 - Given heure après cut-off, When créer Shipment, Then date = prochain jour ouvré")
    void testCreateShipmentAfterCutoff() {
        // Given - Simuler heure après 14h
        ShipmentRequestDto request = new ShipmentRequestDto();
        request.setSalesOrderId(salesOrder.getId());
        request.setCarrierId(carrier.getId());
        request.setCutoffHour(10); // Cut-off à 10h, donc forcément après maintenant

        when(salesOrderRepository.findById(salesOrder.getId())).thenReturn(Optional.of(salesOrder));
        when(carrierRepository.findById(carrier.getId())).thenReturn(Optional.of(carrier));
        when(shipmentRepository.countByPlannedDepartureDateBetween(any(), any())).thenReturn(0L);
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> {
            Shipment s = invocation.getArgument(0);
            s.setId(UUID.randomUUID());
            return s;
        });

        // When
        ShipmentResponseDto result = shipmentService.createShipment(request);

        // Then
        assertNotNull(result.getPlannedDepartureDate());
        LocalDateTime plannedDate = result.getPlannedDepartureDate()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        // Vérifier que c'est un jour ouvré (pas samedi/dimanche)
        DayOfWeek dayOfWeek = plannedDate.getDayOfWeek();
        assertNotEquals(DayOfWeek.SATURDAY, dayOfWeek);
        assertNotEquals(DayOfWeek.SUNDAY, dayOfWeek);
    }

    @Test
    @DisplayName("US11 - Given commande RESERVED, When marquer SHIPPED, Then statut → SHIPPED et OUTBOUND créés")
    void testMarkShipmentAsShipped() {
        // Given
        Shipment shipment = new Shipment();
        shipment.setId(UUID.randomUUID());
        shipment.setSalesOrder(salesOrder);
        shipment.setCarrier(carrier);
        shipment.setStatus(ShipmentStatus.PLANNED);
        shipment.setTrackingNumber("DHL-12345");

        when(shipmentRepository.findById(shipment.getId())).thenReturn(Optional.of(shipment));
        when(inventoryRepository.findByProduct_IdAndWarehouse_Id(product.getId(), warehouse.getId()))
                .thenReturn(List.of(inventory));
        when(shipmentRepository.save(any(Shipment.class))).thenReturn(shipment);

        // When
        ShipmentResponseDto result = shipmentService.markAsShipped(shipment.getId());

        // Then
        assertEquals(ShipmentStatus.SHIPPED, result.getStatus());
        assertEquals(SOStatus.SHIPPED, salesOrder.getStatus());
        assertNotNull(result.getActualDepartureDate());
        assertNotNull(result.getShippedAt());

        // Vérifier que le mouvement OUTBOUND a été créé
        verify(inventoryMovementRepository, times(1)).save(any(InventoryMovement.class));

        // Vérifier que l'inventaire a été mis à jour
        assertEquals(0L, inventory.getQtyReserved());
        assertEquals(90L, inventory.getQtyOnHand()); // 100 - 10
        verify(inventoryRepository, times(1)).save(inventory);

        // Vérifier que qtyReserved de la ligne est à 0
        assertEquals(0, salesOrderLine.getQtyReserved());
    }

    @Test
    @DisplayName("US11 - Given Shipment PLANNED, When marquer SHIPPED, Then mouvements OUTBOUND avec bonne référence")
    void testMarkShipmentAsShippedCreatesOutboundMovements() {
        // Given
        Shipment shipment = new Shipment();
        shipment.setId(UUID.randomUUID());
        shipment.setSalesOrder(salesOrder);
        shipment.setCarrier(carrier);
        shipment.setStatus(ShipmentStatus.PLANNED);

        when(shipmentRepository.findById(shipment.getId())).thenReturn(Optional.of(shipment));
        when(inventoryRepository.findByProduct_IdAndWarehouse_Id(product.getId(), warehouse.getId()))
                .thenReturn(List.of(inventory));

        // When
        shipmentService.markAsShipped(shipment.getId());

        // Then
        ArgumentCaptor<InventoryMovement> movementCaptor = ArgumentCaptor.forClass(InventoryMovement.class);
        verify(inventoryMovementRepository).save(movementCaptor.capture());

        InventoryMovement movement = movementCaptor.getValue();
        assertEquals(MovementType.OUTBOUND, movement.getType());
        assertEquals(10L, movement.getQuantity());
        assertTrue(movement.getReferenceDoc().contains("SHIPMENT-"));
        assertEquals(product, movement.getProduct());
        assertEquals(warehouse, movement.getWarehouse());
    }

    @Test
    @DisplayName("US11 - Given Shipment non PLANNED, When marquer SHIPPED, Then BusinessRuleException")
    void testMarkShipmentAsShippedWhenNotPlanned() {
        // Given
        Shipment shipment = new Shipment();
        shipment.setId(UUID.randomUUID());
        shipment.setSalesOrder(salesOrder);
        shipment.setStatus(ShipmentStatus.SHIPPED); // Déjà SHIPPED

        when(shipmentRepository.findById(shipment.getId())).thenReturn(Optional.of(shipment));

        // When & Then
        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> shipmentService.markAsShipped(shipment.getId())
        );

        assertTrue(exception.getMessage().contains("PLANNED"));
    }

    @Test
    @DisplayName("US11 - Given Shipment SHIPPED, When marquer DELIVERED, Then statuts → DELIVERED")
    void testMarkShipmentAsDelivered() {
        // Given
        Shipment shipment = new Shipment();
        shipment.setId(UUID.randomUUID());
        shipment.setSalesOrder(salesOrder);
        shipment.setCarrier(carrier);
        shipment.setStatus(ShipmentStatus.SHIPPED);
        shipment.setTrackingNumber("DHL-12345");

        when(shipmentRepository.findById(shipment.getId())).thenReturn(Optional.of(shipment));
        when(shipmentRepository.save(any(Shipment.class))).thenReturn(shipment);

        // When
        ShipmentResponseDto result = shipmentService.markAsDelivered(shipment.getId());

        // Then
        assertEquals(ShipmentStatus.DELIVERED, result.getStatus());
        assertEquals(SOStatus.DELIVERED, salesOrder.getStatus());
        assertNotNull(result.getDeliveredAt());

        verify(shipmentRepository, times(1)).save(shipment);
        verify(salesOrderRepository, times(1)).save(salesOrder);
    }

    @Test
    @DisplayName("US11 - Given Shipment non SHIPPED, When marquer DELIVERED, Then BusinessRuleException")
    void testMarkShipmentAsDeliveredWhenNotShipped() {
        // Given
        Shipment shipment = new Shipment();
        shipment.setId(UUID.randomUUID());
        shipment.setSalesOrder(salesOrder);
        shipment.setStatus(ShipmentStatus.PLANNED); // Pas encore SHIPPED

        when(shipmentRepository.findById(shipment.getId())).thenReturn(Optional.of(shipment));

        // When & Then
        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> shipmentService.markAsDelivered(shipment.getId())
        );

        assertTrue(exception.getMessage().contains("SHIPPED"));
    }

    @Test
    @DisplayName("Given transporteur inactif, When créer Shipment, Then BusinessRuleException")
    void testCreateShipmentWithInactiveCarrier() {
        // Given
        carrier.setActive(false);
        ShipmentRequestDto request = new ShipmentRequestDto();
        request.setSalesOrderId(salesOrder.getId());
        request.setCarrierId(carrier.getId());

        when(salesOrderRepository.findById(salesOrder.getId())).thenReturn(Optional.of(salesOrder));
        when(carrierRepository.findById(carrier.getId())).thenReturn(Optional.of(carrier));

        // When & Then
        BusinessRuleException exception = assertThrows(
                BusinessRuleException.class,
                () -> shipmentService.createShipment(request)
        );

        assertTrue(exception.getMessage().contains("pas actif"));
    }

    @Test
    @DisplayName("Given commande inexistante, When créer Shipment, Then ResourceNotFoundException")
    void testCreateShipmentWithNonExistentOrder() {
        // Given
        ShipmentRequestDto request = new ShipmentRequestDto();
        request.setSalesOrderId(UUID.randomUUID());
        request.setCarrierId(carrier.getId());

        when(salesOrderRepository.findById(any())).thenReturn(Optional.empty());

        // When & Then
        assertThrows(
                ResourceNotFoundException.class,
                () -> shipmentService.createShipment(request)
        );
    }

    @Test
    @DisplayName("Given Shipment inexistant, When marquer SHIPPED, Then ResourceNotFoundException")
    void testMarkNonExistentShipmentAsShipped() {
        // Given
        UUID shipmentId = UUID.randomUUID();
        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(
                ResourceNotFoundException.class,
                () -> shipmentService.markAsShipped(shipmentId)
        );
    }
}
