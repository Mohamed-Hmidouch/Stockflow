package com.example.stockgestion.models.enums;

/**
 * Statuts d'une expédition (Shipment)
 */
public enum ShipmentStatus {
    PLANNED,      // Planifiée, en attente de départ
    SHIPPED,      // En cours d'acheminement
    DELIVERED     // Livré au client final
}