package com.example.stockgestion.models.enums;

/**
 * Statuts d'une commande client (Sales Order)
 */
public enum SOStatus {
    CREATED,    // Créée, en attente de réservation
    RESERVED,   // Stock réservé, en attente d'expédition
    SHIPPED,    // Expédiée, en transit
    DELIVERED,  // Livrée
    CANCELED    // Annulée
}