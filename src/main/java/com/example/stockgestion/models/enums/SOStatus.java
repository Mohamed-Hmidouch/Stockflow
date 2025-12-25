package com.example.stockgestion.models.enums;

/**
 * Statuts d'une commande client (Sales Order)
 */
public enum SOStatus {
    CREATED,             // Créée, en attente de réservation
    RESERVED,            // Stock entièrement réservé, en attente d'expédition
    PARTIALLY_RESERVED,  // Stock partiellement réservé, reste en backorder
    BACKORDERED,         // Toute la commande en attente de stock
    SHIPPED,             // En cours d'expédition
    DELIVERED,           // Livrée
    CANCELED             // Annulée
}