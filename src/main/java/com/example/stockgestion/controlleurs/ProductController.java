package com.example.stockgestion.controlleurs;

import com.example.stockgestion.Dto.request.ProductRequestDto;
import com.example.stockgestion.Dto.response.ProductResponseDto;
import com.example.stockgestion.models.Product;
import com.example.stockgestion.services.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Validated
@Tag(name = "Products", description = "API de gestion des produits - création, consultation, modification et suppression")
public class ProductController {
    private final ProductService productService;

    @Operation(
        summary = "Créer un nouveau produit", 
        description = "Enregistre un nouveau produit dans le système avec toutes ses caractéristiques (nom, SKU, prix, description, etc.)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Produit créé avec succès",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = ProductResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "Données de requête invalides (SKU manquant, prix négatif, etc.)", content = @Content),
        @ApiResponse(responseCode = "409", description = "Produit avec ce SKU existe déjà", content = @Content)
    })
    @PostMapping("/create")
    public ResponseEntity<ProductResponseDto> createProduct(
            @Parameter(description = "Données du produit à créer", required = true)
            @Valid @RequestBody ProductRequestDto requestDto) {
        ProductResponseDto created = productService.createProduct(requestDto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @Operation(
        summary = "Récupérer un produit par ID", 
        description = "Récupère les détails complets d'un produit spécifique par son identifiant unique"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Produit récupéré avec succès",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = ProductResponseDto.class))),
        @ApiResponse(responseCode = "404", description = "Produit introuvable avec cet ID", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDto> getProduct(
            @Parameter(description = "Identifiant unique du produit", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id) {
        ProductResponseDto product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @Operation(
        summary = "Lister tous les produits", 
        description = "Récupère la liste complète de tous les produits enregistrés dans le système avec leurs informations de base"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Liste des produits récupérée avec succès",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = ProductResponseDto.class)))
    })
    @GetMapping
    public ResponseEntity<List<ProductResponseDto>> getAllProducts() {
        List<ProductResponseDto> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @Operation(
        summary = "Mettre à jour partiellement un produit", 
        description = "Met à jour sélectivement les champs d'un produit existant (nom, prix, description, etc.) sans affecter les autres propriétés"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Produit mis à jour avec succès",
            content = @Content(mediaType = "application/json", 
            schema = @Schema(implementation = ProductResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "Données de mise à jour invalides", content = @Content),
        @ApiResponse(responseCode = "404", description = "Produit introuvable", content = @Content)
    })
    @PatchMapping("/update/{id}")
    public ResponseEntity<ProductResponseDto> patchProduct(
            @Parameter(description = "Identifiant unique du produit à modifier", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id,
            @Parameter(description = "Données partielles du produit à mettre à jour", required = true)
            @RequestBody Product product) {
        ProductResponseDto updated = productService.updateProduct(id, product);
        return ResponseEntity.ok(updated);
    }

    @Operation(
        summary = "Supprimer un produit", 
        description = "Supprime définitivement un produit du système. Attention : cette action est irréversible et peut affecter les commandes en cours"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Produit supprimé avec succès", content = @Content),
        @ApiResponse(responseCode = "404", description = "Produit introuvable", content = @Content),
        @ApiResponse(responseCode = "409", description = "Impossible de supprimer - produit utilisé dans des commandes actives", content = @Content)
    })
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "Identifiant unique du produit à supprimer", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}