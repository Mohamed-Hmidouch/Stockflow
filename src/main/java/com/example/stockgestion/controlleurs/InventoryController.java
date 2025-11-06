package com.example.stockgestion.controlleurs;

import com.example.stockgestion.Dto.request.InventoryRequestDto;
import com.example.stockgestion.Dto.response.InventoryResponseDto;
import com.example.stockgestion.services.InventoryService;
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
@RequestMapping("api/inventory")
@AllArgsConstructor
@Validated
@Tag(name = "Inventory", description = "API de gestion des inventaires et mouvements de stock")
public class InventoryController {

    private final InventoryService inventoryService;

    @Operation(
        summary = "Créer un mouvement d'inventaire", 
        description = "Enregistre un nouveau mouvement d'inventaire (entrée/sortie de stock) avec toutes les informations associées"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Mouvement créé avec succès",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = com.example.stockgestion.Dto.response.InventoryMovementResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "Données de requête invalides", content = @Content),
        @ApiResponse(responseCode = "404", description = "Produit ou entrepôt introuvable", content = @Content)
    })
    @PostMapping("/movements/create")
    public ResponseEntity<com.example.stockgestion.Dto.response.InventoryMovementResponseDto> createMovement(
            @Parameter(description = "Données du mouvement d'inventaire à créer", required = true)
            @Valid @RequestBody com.example.stockgestion.Dto.request.InventoryMovementRequestDto request) {
        com.example.stockgestion.Dto.response.InventoryMovementResponseDto dto = inventoryService.createMovement(request);
        return ResponseEntity.status(201).body(dto);
    }

    @Operation(
        summary = "Créer un enregistrement d'inventaire", 
        description = "Crée un nouvel enregistrement d'inventaire pour un produit dans un entrepôt spécifique"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Inventaire créé avec succès",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = InventoryResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "Données de requête invalides", content = @Content),
        @ApiResponse(responseCode = "409", description = "Inventaire déjà existant pour ce produit/entrepôt", content = @Content)
    })
    @PostMapping
    public ResponseEntity<InventoryResponseDto> createInventory(
            @Parameter(description = "Données de l'inventaire à créer", required = true)
            @Valid @RequestBody InventoryRequestDto request) {
        InventoryResponseDto dto = inventoryService.createInventory(request);
        return ResponseEntity.status(201).body(dto);
    }

    @Operation(
        summary = "Lister tous les inventaires", 
        description = "Récupère la liste complète de tous les enregistrements d'inventaire avec les quantités disponibles et réservées"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste des inventaires récupérée avec succès",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = InventoryResponseDto.class)))
    })
    @GetMapping
    public ResponseEntity<List<InventoryResponseDto>> getAll() {
        List<InventoryResponseDto> list = inventoryService.getAllInventories();
        return ResponseEntity.ok(list);
    }

    @Operation(
        summary = "Lister les inventaires par produit", 
        description = "Récupère tous les enregistrements d'inventaire pour un produit spécifique à travers tous les entrepôts"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Inventaires du produit récupérés avec succès",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = InventoryResponseDto.class))),
        @ApiResponse(responseCode = "404", description = "Produit introuvable", content = @Content)
    })
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<InventoryResponseDto>> getByProduct(
            @Parameter(description = "Identifiant unique du produit", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID productId) {
        List<InventoryResponseDto> list = inventoryService.getByProductId(productId);
        return ResponseEntity.ok(list);
    }

    @Operation(
        summary = "Lister les inventaires par entrepôt", 
        description = "Récupère tous les enregistrements d'inventaire pour un entrepôt spécifique à travers tous les produits"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Inventaires de l'entrepôt récupérés avec succès",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = InventoryResponseDto.class))),
        @ApiResponse(responseCode = "404", description = "Entrepôt introuvable", content = @Content)
    })
    @GetMapping("/warehouse/{warehouseId}")
    public ResponseEntity<List<InventoryResponseDto>> getByWarehouse(
            @Parameter(description = "Identifiant unique de l'entrepôt", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID warehouseId) {
        List<InventoryResponseDto> list = inventoryService.getByWarehouseId(warehouseId);
        return ResponseEntity.ok(list);
    }

    @Operation(
        summary = "Récupérer un inventaire par ID", 
        description = "Récupère les détails complets d'un enregistrement d'inventaire spécifique"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Inventaire récupéré avec succès",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = InventoryResponseDto.class))),
        @ApiResponse(responseCode = "404", description = "Inventaire introuvable", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<InventoryResponseDto> getById(
            @Parameter(description = "Identifiant unique de l'inventaire", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id) {
        InventoryResponseDto dto = inventoryService.getInventoryById(id);
        return ResponseEntity.ok(dto);
    }

    @Operation(
        summary = "Mettre à jour un inventaire", 
        description = "Met à jour les quantités et informations d'un enregistrement d'inventaire existant"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Inventaire mis à jour avec succès",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = InventoryResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "Données de requête invalides", content = @Content),
        @ApiResponse(responseCode = "404", description = "Inventaire introuvable", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<InventoryResponseDto> update(
            @Parameter(description = "Identifiant unique de l'inventaire", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id, 
            @Parameter(description = "Nouvelles données de l'inventaire", required = true)
            @Valid @RequestBody InventoryRequestDto request) {
        InventoryResponseDto dto = inventoryService.updateInventory(id, request);
        return ResponseEntity.ok(dto);
    }
}
