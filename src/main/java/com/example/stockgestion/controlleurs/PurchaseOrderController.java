package com.example.stockgestion.controlleurs;

import com.example.stockgestion.Dto.request.PurchaseOrderRequestDto;
import com.example.stockgestion.Dto.request.ReceiveRequestDto;
import com.example.stockgestion.Dto.response.PurchaseOrderResponseDto;
import com.example.stockgestion.services.PurchaseOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/purchase-orders")
@AllArgsConstructor
@Validated
@Tag(name = "Purchase Orders", description = "API de gestion des bons de commande d'achat - création, réception et suivi des approvisionnements")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @Operation(
        summary = "Créer un bon de commande d'achat", 
        description = "Crée un nouveau bon de commande d'achat avec les lignes de produits à commander auprès d'un fournisseur"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Bon de commande créé avec succès",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = PurchaseOrderResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "Données de requête invalides (fournisseur manquant, quantités incorrectes, etc.)", content = @Content),
        @ApiResponse(responseCode = "404", description = "Fournisseur ou produit introuvable", content = @Content)
    })
    @PostMapping("/create")
    public ResponseEntity<PurchaseOrderResponseDto> createPurchaseOrder(
            @Parameter(description = "Données du bon de commande à créer avec les lignes de produits", required = true)
            @Valid @RequestBody PurchaseOrderRequestDto request) {
        PurchaseOrderResponseDto response = purchaseOrderService.createPurchaseOrder(request);
        return ResponseEntity.status(201).body(response);
    }

    @Operation(
        summary = "Réceptionner une commande d'achat", 
        description = "Enregistre la réception totale ou partielle d'un bon de commande d'achat et met à jour les stocks en conséquence"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Réception enregistrée avec succès",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = PurchaseOrderResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "Données de réception invalides (quantités négatives, dépassement de commande, etc.)", content = @Content),
        @ApiResponse(responseCode = "404", description = "Bon de commande introuvable", content = @Content),
        @ApiResponse(responseCode = "409", description = "Commande déjà entièrement reçue ou dans un état non réceptionnable", content = @Content)
    })
    @PostMapping("/receive/{orderId}")
    public ResponseEntity<PurchaseOrderResponseDto> receptionOrder(
            @Parameter(description = "Identifiant unique du bon de commande à réceptionner", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID orderId, 
            @Parameter(description = "Détails de la réception avec les quantités reçues par produit", required = true)
            @Valid @RequestBody ReceiveRequestDto request) {
        PurchaseOrderResponseDto response = purchaseOrderService.receptionOrder(orderId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Lister tous les bons de commande d'achat", 
        description = "Récupère la liste complète de tous les bons de commande d'achat avec leur statut actuel (en attente, partiellement reçu, reçu, etc.)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste des bons de commande récupérée avec succès",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = PurchaseOrderResponseDto.class)))
    })
    @GetMapping
    public ResponseEntity<List<PurchaseOrderResponseDto>> getAllPurchaseOrders() {
        List<PurchaseOrderResponseDto> response = purchaseOrderService.getAllPurchaseOrders();
        return ResponseEntity.ok(response);
    }

    @Operation(
        summary = "Récupérer un bon de commande par ID", 
        description = "Récupère les détails complets d'un bon de commande d'achat spécifique avec toutes ses lignes et son historique de réception"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Bon de commande récupéré avec succès",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = PurchaseOrderResponseDto.class))),
        @ApiResponse(responseCode = "404", description = "Bon de commande introuvable avec cet ID", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrderResponseDto> getPurchaseOrderById(
            @Parameter(description = "Identifiant unique du bon de commande", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id) {
        PurchaseOrderResponseDto response = purchaseOrderService.getPurchaseOrderById(id);
        return ResponseEntity.ok(response);
    }
}