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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service pour la gestion des expéditions (Shipments)
 * US10 - Créer l'expédition
 * US11 - Marquer la commande SHIPPED puis DELIVERED
 */
@Service
@RequiredArgsConstructor
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final CarrierRepository carrierRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryMovmentRepository inventoryMovementRepository;

    private static final int DEFAULT_CUTOFF_HOUR = 14; // 14h
    private static final int MAX_SHIPMENTS_PER_SLOT = 10; // Capacité maximale par créneau

    /**
     * US10 - Créer une expédition pour une commande RESERVED
     * Critères :
     * - Given une commande RESERVED, When je crée le Shipment, Then status = PLANNED et commande reste RESERVED
     * - Given l'heure après le cut-off, When je planifie, Then date de départ = prochain jour ouvré
     */
    @Transactional
    public ShipmentResponseDto createShipment(ShipmentRequestDto request) {
        // 1. Vérifier que la commande existe et est RESERVED
        SalesOrder salesOrder = salesOrderRepository.findById(request.getSalesOrderId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Commande non trouvée avec l'ID : " + request.getSalesOrderId()));

        if (salesOrder.getStatus() != SOStatus.RESERVED) {
            throw new BusinessRuleException(
                    "La commande doit être RESERVED pour créer une expédition. Statut actuel : " + salesOrder.getStatus());
        }

        // 2. Vérifier que le transporteur existe
        Carrier carrier = carrierRepository.findById(request.getCarrierId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transporteur non trouvé avec l'ID : " + request.getCarrierId()));

        if (!carrier.isActive()) {
            throw new BusinessRuleException("Le transporteur " + carrier.getName() + " n'est pas actif");
        }

        // 3. Calculer la date de départ planifiée selon le cut-off
        int cutoffHour = request.getCutoffHour() != null ? request.getCutoffHour() : DEFAULT_CUTOFF_HOUR;
        Instant plannedDepartureDate = calculatePlannedDepartureDate(cutoffHour);

        // 4. Vérifier la capacité du slot d'expédition
        plannedDepartureDate = checkAndAdjustSlotCapacity(plannedDepartureDate);

        // 5. Créer le Shipment avec status PLANNED
        Shipment shipment = new Shipment();
        shipment.setSalesOrder(salesOrder);
        shipment.setCarrier(carrier);
        shipment.setStatus(ShipmentStatus.PLANNED);
        shipment.setPlannedDepartureDate(plannedDepartureDate);
        shipment.setCutoffHour(cutoffHour);
        
        // Générer un numéro de suivi si fourni
        if (request.getTrackingNumber() != null && !request.getTrackingNumber().isBlank()) {
            shipment.setTrackingNumber(request.getTrackingNumber());
        } else {
            shipment.setTrackingNumber(generateTrackingNumber(carrier));
        }

        shipment = shipmentRepository.save(shipment);

        // La commande reste RESERVED (selon le brief)
        return new ShipmentResponseDto(shipment);
    }

    /**
     * US11 - Marquer l'expédition comme SHIPPED
     * Critères :
     * - Given une commande RESERVED, When l'expédition démarre
     * - Then status commande → SHIPPED, OUTBOUND créés, qtyReserved diminue
     */
    @Transactional
    public ShipmentResponseDto markAsShipped(UUID shipmentId) {
        // 1. Charger le Shipment
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Expédition non trouvée avec l'ID : " + shipmentId));

        // 2. Vérifier que le Shipment est PLANNED
        if (shipment.getStatus() != ShipmentStatus.PLANNED) {
            throw new BusinessRuleException(
                    "L'expédition doit être PLANNED pour être marquée comme SHIPPED. Statut actuel : " + shipment.getStatus());
        }

        // 3. Vérifier que la commande est RESERVED
        SalesOrder salesOrder = shipment.getSalesOrder();
        if (salesOrder.getStatus() != SOStatus.RESERVED) {
            throw new BusinessRuleException(
                    "La commande doit être RESERVED. Statut actuel : " + salesOrder.getStatus());
        }

        // 4. Changer le status du Shipment à SHIPPED
        shipment.setStatus(ShipmentStatus.SHIPPED);
        shipment.setActualDepartureDate(Instant.now());
        shipment.setShippedAt(Instant.now()); // Pour compatibilité

        // 5. Changer le status de la SalesOrder à SHIPPED
        salesOrder.setStatus(SOStatus.SHIPPED);

        // 6. Créer les mouvements OUTBOUND et diminuer qtyReserved
        createOutboundMovements(salesOrder, shipment);

        shipmentRepository.save(shipment);
        salesOrderRepository.save(salesOrder);

        return new ShipmentResponseDto(shipment);
    }

    /**
     * US11 - Marquer l'expédition comme DELIVERED
     * Critères :
     * - Given un avis de livraison, When je reçois la preuve
     * - Then commande → DELIVERED et Shipment → DELIVERED
     */
    @Transactional
    public ShipmentResponseDto markAsDelivered(UUID shipmentId) {
        // 1. Charger le Shipment
        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Expédition non trouvée avec l'ID : " + shipmentId));

        // 2. Vérifier que le Shipment est SHIPPED
        if (shipment.getStatus() != ShipmentStatus.SHIPPED) {
            throw new BusinessRuleException(
                    "L'expédition doit être SHIPPED pour être marquée comme DELIVERED. Statut actuel : " + shipment.getStatus());
        }

        // 3. Marquer comme DELIVERED
        shipment.setStatus(ShipmentStatus.DELIVERED);
        shipment.setDeliveredAt(Instant.now());

        // 4. Marquer la commande comme DELIVERED
        SalesOrder salesOrder = shipment.getSalesOrder();
        salesOrder.setStatus(SOStatus.DELIVERED);

        shipmentRepository.save(shipment);
        salesOrderRepository.save(salesOrder);

        return new ShipmentResponseDto(shipment);
    }

    /**
     * Calculer la date de départ planifiée selon le cut-off
     * - Si avant le cut-off → même jour
     * - Si après le cut-off → prochain jour ouvré
     */
    private Instant calculatePlannedDepartureDate(int cutoffHour) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime plannedDate;

        if (now.getHour() >= cutoffHour) {
            // Après le cut-off → prochain jour ouvré
            plannedDate = getNextBusinessDay(now.toLocalDate()).atTime(9, 0);
        } else {
            // Avant le cut-off → même jour à l'heure du cut-off
            plannedDate = now.toLocalDate().atTime(cutoffHour, 0);
        }

        return plannedDate.atZone(ZoneId.systemDefault()).toInstant();
    }

    /**
     * Obtenir le prochain jour ouvré (lundi-vendredi)
     */
    private LocalDate getNextBusinessDay(LocalDate date) {
        LocalDate next = date.plusDays(1);

        // Si c'est samedi (6) ou dimanche (7), aller au lundi
        while (next.getDayOfWeek() == DayOfWeek.SATURDAY ||
                next.getDayOfWeek() == DayOfWeek.SUNDAY) {
            next = next.plusDays(1);
        }

        return next;
    }

    /**
     * Vérifier la capacité du slot d'expédition
     * Si le slot est plein → replanifier au prochain slot
     */
    private Instant checkAndAdjustSlotCapacity(Instant plannedDate) {
        LocalDate plannedLocalDate = plannedDate.atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDateTime startOfDay = plannedLocalDate.atStartOfDay();
        LocalDateTime endOfDay = plannedLocalDate.atTime(23, 59, 59);

        Instant startInstant = startOfDay.atZone(ZoneId.systemDefault()).toInstant();
        Instant endInstant = endOfDay.atZone(ZoneId.systemDefault()).toInstant();

        // Compter les shipments planifiés pour ce jour
        long count = shipmentRepository.countByPlannedDepartureDateBetween(startInstant, endInstant);

        if (count >= MAX_SHIPMENTS_PER_SLOT) {
            // Slot plein → replanifier au prochain jour ouvré
            LocalDate nextDay = getNextBusinessDay(plannedLocalDate);
            return nextDay.atTime(DEFAULT_CUTOFF_HOUR, 0)
                    .atZone(ZoneId.systemDefault())
                    .toInstant();
        }

        return plannedDate;
    }

    /**
     * Créer les mouvements OUTBOUND et diminuer qtyReserved
     */
    private void createOutboundMovements(SalesOrder salesOrder, Shipment shipment) {
        for (SalesOrderLine line : salesOrder.getLines()) {
            if (line.getQtyReserved() <= 0) {
                continue; // Pas de quantité réservée à expédier
            }

            // Créer le mouvement OUTBOUND
            InventoryMovement movement = new InventoryMovement();
            movement.setType(MovementType.OUTBOUND);
            movement.setProduct(line.getProduct());
            movement.setWarehouse(line.getWarehouse());
            movement.setQuantity(line.getQtyReserved());
            movement.setOccurredAt(Instant.now());
            movement.setReferenceDoc("SHIPMENT-" + shipment.getId());

            inventoryMovementRepository.save(movement);

            // Mettre à jour l'inventaire
            updateInventoryAfterShipment(line);
        }
    }

    /**
     * Mettre à jour l'inventaire après expédition
     * - Diminuer qtyReserved
     * - Diminuer qtyOnHand (le stock quitte physiquement l'entrepôt)
     */
    private void updateInventoryAfterShipment(SalesOrderLine line) {
        List<Inventory> inventories = inventoryRepository
                .findByProduct_IdAndWarehouse_Id(line.getProduct().getId(), line.getWarehouse().getId());
        
        if (inventories.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Inventaire non trouvé pour produit " + line.getProduct().getSku() +
                            " dans l'entrepôt " + line.getWarehouse().getCode());
        }
        
        Inventory inventory = inventories.get(0);

        // Diminuer qtyReserved
        long newQtyReserved = inventory.getQtyReserved() - line.getQtyReserved();
        if (newQtyReserved < 0) {
            throw new BusinessRuleException(
                    "Quantité réservée insuffisante pour " + line.getProduct().getSku());
        }
        inventory.setQtyReserved(newQtyReserved);

        // Diminuer qtyOnHand (stock physique qui quitte l'entrepôt)
        long newQtyOnHand = inventory.getQtyOnHand() - line.getQtyReserved();
        if (newQtyOnHand < 0) {
            throw new BusinessRuleException(
                    "Quantité physique insuffisante pour " + line.getProduct().getSku());
        }
        inventory.setQtyOnHand(newQtyOnHand);

        // qtyAvailable = qtyOnHand - qtyReserved (calculé au runtime si nécessaire)

        inventoryRepository.save(inventory);

        // Remettre qtyReserved de la ligne à 0 (expédié)
        line.setQtyReserved(0);
    }

    /**
     * Générer un numéro de suivi unique
     */
    private String generateTrackingNumber(Carrier carrier) {
        String prefix = carrier.getName().substring(0, Math.min(3, carrier.getName().length())).toUpperCase();
        String timestamp = String.valueOf(System.currentTimeMillis());
        return prefix + "-" + timestamp.substring(timestamp.length() - 8);
    }

    // ===== Méthodes de consultation =====

    public List<ShipmentResponseDto> findAll() {
        return shipmentRepository.findAll().stream()
                .map(ShipmentResponseDto::new)
                .collect(Collectors.toList());
    }

    public ShipmentResponseDto findById(UUID id) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Expédition non trouvée avec l'ID : " + id));
        return new ShipmentResponseDto(shipment);
    }

    public List<ShipmentResponseDto> findBySalesOrderId(UUID salesOrderId) {
        List<Shipment> shipments = shipmentRepository.findBySalesOrderId(salesOrderId);
        return shipments.stream()
                .map(ShipmentResponseDto::new)
                .collect(Collectors.toList());
    }

    public List<ShipmentResponseDto> findByStatus(ShipmentStatus status) {
        List<Shipment> shipments = shipmentRepository.findByStatus(status);
        return shipments.stream()
                .map(ShipmentResponseDto::new)
                .collect(Collectors.toList());
    }
}
