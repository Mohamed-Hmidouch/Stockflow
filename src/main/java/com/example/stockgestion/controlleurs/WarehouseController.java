package com.example.stockgestion.controlleurs;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.stockgestion.Dto.request.WareHouseRequestDto;
import com.example.stockgestion.Dto.response.WareHouseResponseDto;
import com.example.stockgestion.services.WarehouseService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/warehouses")
@RequiredArgsConstructor
@Validated
@Tag(name = "Warehouses", description = "API de gestion des entrepôts - création, consultation et administration des sites de stockage")
public class WarehouseController {
    private final WarehouseService warehouseService;

    @Operation(
        summary = "Lister tous les entrepôts", 
        description = "Récupère la liste complète de tous les entrepôts enregistrés dans le système avec leurs informations (nom, adresse, capacité, etc.)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste des entrepôts récupérée avec succès",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = WareHouseResponseDto.class)))
    })
    @GetMapping
    public ResponseEntity<List<WareHouseResponseDto>> getAllWarehouses(){
        List<WareHouseResponseDto> warehouses = warehouseService.getAllWareHouses();
        return ResponseEntity.ok(warehouses);
    }
    
    @Operation(
        summary = "Créer un nouveau entrepôt", 
        description = "Enregistre un nouvel entrepôt dans le système avec ses caractéristiques (nom, code, adresse, capacité, responsable, etc.)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Entrepôt créé avec succès",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = WareHouseResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "Données de requête invalides (nom/code manquant, format adresse incorrect, etc.)", content = @Content),
        @ApiResponse(responseCode = "409", description = "Entrepôt avec ce code existe déjà", content = @Content)
    })
    @PostMapping("create")
    public ResponseEntity<WareHouseResponseDto> createWarehouse(
            @Parameter(description = "Données de l'entrepôt à créer", required = true)
            @RequestBody @Valid WareHouseRequestDto requestDto) {
        WareHouseResponseDto createdWarehouse = warehouseService.createWareHouse(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdWarehouse);
    }

    @Operation(
        summary = "Récupérer un entrepôt par ID", 
        description = "Récupère les détails complets d'un entrepôt spécifique par son identifiant unique, incluant ses statistiques d'inventaire"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Entrepôt récupéré avec succès",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = WareHouseResponseDto.class))),
        @ApiResponse(responseCode = "404", description = "Entrepôt introuvable avec cet ID", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<WareHouseResponseDto> getWarehouseById(
            @Parameter(description = "Identifiant unique de l'entrepôt", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id) {
        WareHouseResponseDto warehouse = warehouseService.getWarehouseById(id);
        return ResponseEntity.ok(warehouse);
    }
}