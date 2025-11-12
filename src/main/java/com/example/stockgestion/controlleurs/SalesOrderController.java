package com.example.stockgestion.controlleurs;

import com.example.stockgestion.Dto.request.SalesOrderRequestDto;
import com.example.stockgestion.Dto.response.SalesOrderResponseDto;
import com.example.stockgestion.services.SalesOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/sales-orders")
@RequiredArgsConstructor
@Validated
@Tag(name = "Sales Orders", description = "API de gestion des commandes clients - création, consultation, annulation et expédition des commandes")
public class SalesOrderController {

    private final SalesOrderService salesOrderService;

    @GetMapping
    public ResponseEntity<List<SalesOrderResponseDto>> getAllSalesOrders() {
        List<SalesOrderResponseDto> orders = salesOrderService.getAllSalesOrders();
        return ResponseEntity.ok(orders);
    }

    @Operation(
        summary = "Créer une nouvelle commande client", 
        description = """
                Crée une nouvelle commande client avec les lignes de commande spécifiées.
                La commande est automatiquement créée avec le statut DRAFT et les stocks sont réservés si disponibles.
                Les articles en rupture de stock sont mis en backorder automatiquement.
                """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Commande créée avec succès",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = SalesOrderResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "Données de requête invalides (client inexistant, produits introuvables, quantités négatives, etc.)", content = @Content),
        @ApiResponse(responseCode = "422", description = "Impossible de créer la commande (contraintes métier non respectées)", content = @Content)
    })
    @PostMapping("/create")
    public ResponseEntity<SalesOrderResponseDto> createSalesOrder(
            @Parameter(description = "Données de la commande client à créer avec ses lignes", required = true)
            @Valid @RequestBody SalesOrderRequestDto request) {
        SalesOrderResponseDto created = salesOrderService.createSalesOrder(request);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @Operation(
        summary = "Récupérer une commande client par ID", 
        description = """
                Récupère les détails complets d'une commande client spécifique avec toutes ses lignes de commande,
                les informations de stock réservé/en backorder, et l'historique des statuts.
                """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Commande récupérée avec succès",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = SalesOrderResponseDto.class))),
        @ApiResponse(responseCode = "404", description = "Commande introuvable avec cet ID", content = @Content)
    })
    @GetMapping("/{orderId}")
    public ResponseEntity<SalesOrderResponseDto> getSalesOrder(
            @Parameter(description = "Identifiant unique de la commande client", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID orderId) {
        SalesOrderResponseDto order = salesOrderService.getSalesOrderById(orderId);
        return ResponseEntity.ok(order);
    }

    @Operation(
        summary = "Annuler une commande client", 
        description = """
                Annule une commande client en cours. Cette action :
                - Libère tous les stocks réservés pour cette commande
                - Supprime les éléments en backorder
                - Change le statut de la commande vers CANCELLED
                - Cette action est irréversible
                
                ⚠️ Seules les commandes en statut DRAFT ou CONFIRMED peuvent être annulées.
                """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Commande annulée avec succès",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = SalesOrderResponseDto.class))),
        @ApiResponse(responseCode = "404", description = "Commande introuvable", content = @Content),
        @ApiResponse(responseCode = "409", description = "Impossible d'annuler - commande déjà expédiée ou dans un état non annulable", content = @Content)
    })
    @PutMapping("/{orderId}/cancel")
    public ResponseEntity<SalesOrderResponseDto> cancelSalesOrder(
            @Parameter(description = "Identifiant unique de la commande à annuler", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID orderId) {
        SalesOrderResponseDto cancelled = salesOrderService.cancelOrder(orderId);
        return ResponseEntity.ok(cancelled);
    }

    @Operation(
        summary = "Expédier une commande client", 
        description = """
                Marque une commande comme expédiée et met à jour les stocks. Cette action :
                - Change le statut de la commande vers SHIPPED
                - Décrémente définitivement les stocks des produits
                - Libère les réservations de stock
                - Génère un événement d'expédition pour le suivi
                
                ⚠️ Seules les commandes en statut CONFIRMED peuvent être expédiées.
                ⚠️ Tous les produits doivent être disponibles en stock (pas de backorder).
                """
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Commande expédiée avec succès",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = SalesOrderResponseDto.class))),
        @ApiResponse(responseCode = "404", description = "Commande introuvable", content = @Content),
        @ApiResponse(responseCode = "409", description = "Impossible d'expédier - commande dans un mauvais état ou stock insuffisant", content = @Content),
        @ApiResponse(responseCode = "422", description = "Commande contient des articles en backorder - expédition impossible", content = @Content)
    })
    @PutMapping("/{orderId}/ship")
    public ResponseEntity<SalesOrderResponseDto> shipSalesOrder(
            @Parameter(description = "Identifiant unique de la commande à expédier", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID orderId) {
        SalesOrderResponseDto shipped = salesOrderService.shipOrder(orderId);
        return ResponseEntity.ok(shipped);
    }
}