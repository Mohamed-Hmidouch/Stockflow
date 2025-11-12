package com.example.stockgestion.controlleurs;

import com.example.stockgestion.Dto.request.SupplierRequestDto;
import com.example.stockgestion.Dto.response.SupplierResponseDto;
import com.example.stockgestion.services.SupplierService;
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
@RequestMapping("/api/suppliers")
@RequiredArgsConstructor
@Validated
@Tag(name = "Suppliers", description = "API de gestion des fournisseurs - création, consultation, modification et suppression des partenaires d'approvisionnement")
public class SupplierController {
    
    private final SupplierService supplierService;

    @Operation(
        summary = "Lister tous les fournisseurs", 
        description = "Récupère la liste complète de tous les fournisseurs enregistrés dans le système avec leurs informations de contact"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste des fournisseurs récupérée avec succès",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = SupplierResponseDto.class)))
    })
    @GetMapping
    public ResponseEntity<List<SupplierResponseDto>> getAllSuppliers() {
        List<SupplierResponseDto> suppliers = supplierService.getAllSuppliers();
        return ResponseEntity.ok(suppliers);
    }

    @Operation(
        summary = "Créer un nouveau fournisseur", 
        description = "Enregistre un nouveau fournisseur dans le système avec ses informations de contact et ses détails d'identification"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Fournisseur créé avec succès",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = SupplierResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "Données de requête invalides (nom manquant, format incorrect, etc.)", content = @Content),
        @ApiResponse(responseCode = "409", description = "Fournisseur avec ce nom existe déjà", content = @Content)
    })
    @PostMapping("/create")
    public ResponseEntity<SupplierResponseDto> createSupplier(
            @Parameter(description = "Données du fournisseur à créer", required = true)
            @Valid @RequestBody SupplierRequestDto requestDto) {
        SupplierResponseDto created = supplierService.createSupplier(requestDto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @Operation(
        summary = "Récupérer un fournisseur par ID", 
        description = "Récupère les détails complets d'un fournisseur spécifique par son identifiant unique"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Fournisseur récupéré avec succès",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = SupplierResponseDto.class))),
        @ApiResponse(responseCode = "404", description = "Fournisseur introuvable avec cet ID", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<SupplierResponseDto> getSupplier(
            @Parameter(description = "Identifiant unique du fournisseur", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id) {
        SupplierResponseDto supplier = supplierService.getSupplierById(id);
        return ResponseEntity.ok(supplier);
    }

    @Operation(
        summary = "Mettre à jour un fournisseur", 
        description = "Met à jour les informations d'un fournisseur existant (nom, contact, etc.)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Fournisseur mis à jour avec succès",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = SupplierResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "Données de mise à jour invalides", content = @Content),
        @ApiResponse(responseCode = "404", description = "Fournisseur introuvable", content = @Content),
        @ApiResponse(responseCode = "409", description = "Nom de fournisseur déjà utilisé", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<SupplierResponseDto> updateSupplier(
            @Parameter(description = "Identifiant unique du fournisseur à modifier", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id,
            @Parameter(description = "Nouvelles données du fournisseur", required = true)
            @Valid @RequestBody SupplierRequestDto requestDto) {
        SupplierResponseDto updated = supplierService.updateSupplier(id, requestDto);
        return ResponseEntity.ok(updated);
    }

    @Operation(
        summary = "Supprimer un fournisseur", 
        description = "Supprime définitivement un fournisseur du système. Attention : cette action peut affecter les commandes d'approvisionnement associées"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Fournisseur supprimé avec succès", content = @Content),
        @ApiResponse(responseCode = "404", description = "Fournisseur introuvable", content = @Content),
        @ApiResponse(responseCode = "409", description = "Impossible de supprimer - fournisseur utilisé dans des commandes actives", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSupplier(
            @Parameter(description = "Identifiant unique du fournisseur à supprimer", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id) {
        supplierService.deleteSupplier(id);
        return ResponseEntity.noContent().build();
    }
}