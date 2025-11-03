package com.example.stockgestion.services.helpers;

import com.example.stockgestion.exception.BusinessRuleException;
import com.example.stockgestion.exception.ResourceNotFoundException;
import com.example.stockgestion.models.Product;
import com.example.stockgestion.repositories.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Helper pour valider les produits
 */
@Component
@AllArgsConstructor
public class ProductValidator {
    private final ProductRepository productRepository;

    /**
     * Valide et récupère un produit par son ID
     */
    public Product validateAndGet(UUID productId) {
        return productRepository.findById(productId)
            .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
    }

    /**
     * Valide et récupère le prix d'un produit
     */
    public BigDecimal validateAndGetPrice(Product product) {
        if (product.getPrice() == null) {
            throw new BusinessRuleException("Le produit '" + product.getName() + "' n'a pas de prix défini");
        }
        return product.getPrice();
    }
}
