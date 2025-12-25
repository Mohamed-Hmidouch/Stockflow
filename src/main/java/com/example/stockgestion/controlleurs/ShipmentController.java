package com.example.stockgestion.controlleurs;

import com.example.stockgestion.Dto.request.ShipmentRequestDto;
import com.example.stockgestion.Dto.response.ShipmentResponseDto;
import com.example.stockgestion.models.enums.ShipmentStatus;
import com.example.stockgestion.services.ShipmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Contrôleur pour la gestion des expéditions (Shipments)
 * US10 - Créer l'expédition
 * US11 - Marquer la commande SHIPPED puis DELIVERED
 */
@RestController
@RequestMapping("/api/shipments")
@RequiredArgsConstructor
@Tag(name = "Shipments", description = "Gestion des expéditions")
public class ShipmentController {

    private final ShipmentService shipmentService;

    /**
     * US10 - Créer une expédition pour une commande RESERVED
     */
    @PostMapping
    @Operation(
            summary = "US10 - Créer une expédition",
            description = "Créer une expédition pour une commande RESERVED. " +
                    "Le statut de l'expédition sera PLANNED. " +
                    "Si l'heure est après le cut-off, la date de départ sera le prochain jour ouvré."
    )
    public ResponseEntity<ShipmentResponseDto> createShipment(
            @Valid @RequestBody ShipmentRequestDto request) {
        ShipmentResponseDto response = shipmentService.createShipment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * US11 - Marquer l'expédition comme SHIPPED
     */
    @PutMapping("/{id}/ship")
    @Operation(
            summary = "US11 - Marquer l'expédition comme SHIPPED",
            description = "Démarre l'expédition : " +
                    "- Change le statut du Shipment à SHIPPED\n" +
                    "- Change le statut de la SalesOrder à SHIPPED\n" +
                    "- Crée les mouvements OUTBOUND\n" +
                    "- Diminue qtyReserved dans l'inventaire"
    )
    public ResponseEntity<ShipmentResponseDto> markAsShipped(@PathVariable UUID id) {
        ShipmentResponseDto response = shipmentService.markAsShipped(id);
        return ResponseEntity.ok(response);
    }

    /**
     * US11 - Marquer l'expédition comme DELIVERED
     */
    @PutMapping("/{id}/deliver")
    @Operation(
            summary = "US11 - Marquer l'expédition comme DELIVERED",
            description = "Marque l'expédition et la commande comme livrées"
    )
    public ResponseEntity<ShipmentResponseDto> markAsDelivered(@PathVariable UUID id) {
        ShipmentResponseDto response = shipmentService.markAsDelivered(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Récupérer toutes les expéditions
     */
    @GetMapping
    @Operation(summary = "Lister toutes les expéditions")
    public ResponseEntity<List<ShipmentResponseDto>> getAllShipments() {
        List<ShipmentResponseDto> shipments = shipmentService.findAll();
        return ResponseEntity.ok(shipments);
    }

    /**
     * Récupérer une expédition par ID
     */
    @GetMapping("/{id}")
    @Operation(summary = "Récupérer une expédition par ID")
    public ResponseEntity<ShipmentResponseDto> getShipmentById(@PathVariable UUID id) {
        ShipmentResponseDto shipment = shipmentService.findById(id);
        return ResponseEntity.ok(shipment);
    }

    /**
     * Récupérer les expéditions d'une commande
     */
    @GetMapping("/sales-order/{salesOrderId}")
    @Operation(summary = "Récupérer les expéditions d'une commande")
    public ResponseEntity<List<ShipmentResponseDto>> getShipmentsBySalesOrder(
            @PathVariable UUID salesOrderId) {
        List<ShipmentResponseDto> shipments = shipmentService.findBySalesOrderId(salesOrderId);
        return ResponseEntity.ok(shipments);
    }

    /**
     * Récupérer les expéditions par statut
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "Récupérer les expéditions par statut")
    public ResponseEntity<List<ShipmentResponseDto>> getShipmentsByStatus(
            @PathVariable ShipmentStatus status) {
        List<ShipmentResponseDto> shipments = shipmentService.findByStatus(status);
        return ResponseEntity.ok(shipments);
    }
}
