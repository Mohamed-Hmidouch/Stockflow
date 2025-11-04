package com.example.stockgestion.models.enums;

/**
 * Statuts d'un bon de commande d'achat (Purchase Order)
 */
public enum POStatus {
    DRAFT,      // Brouillon, en cours de création
    APPROVED,   // Approuvé, prêt à être envoyé au fournisseur
    RECEIVED,   // Reçu (peut être partiel ou total)
    PARTIALLY_RECEIVED, // Partiellement reçu
    CANCELED    // Annulé
}