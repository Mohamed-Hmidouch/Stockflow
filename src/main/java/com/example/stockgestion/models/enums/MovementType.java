package com.example.stockgestion.models.enums;

/**
 * Type de mouvement de stock
 */
public enum MovementType {
    INBOUND,      // Entrée de stock (ex: réception d'un PurchaseOrder)
    OUTBOUND,     // Sortie de stock (ex: expédition d'un SalesOrder)
    ADJUSTMENT    // Correction manuelle (ex: inventaire physique)
}