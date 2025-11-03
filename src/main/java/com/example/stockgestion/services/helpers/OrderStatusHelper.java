package com.example.stockgestion.services.helpers;

import com.example.stockgestion.models.enums.SOStatus;
import org.springframework.stereotype.Component;

/**
 * Helper pour déterminer le statut d'une commande
 */
@Component
public class OrderStatusHelper {

    /**
     * Détermine le statut final d'une commande en fonction du backorder et réservation
     */
    public SOStatus determineStatus(boolean hasBackorder, boolean hasReserved) {
        if (hasBackorder && hasReserved) {
            return SOStatus.PARTIALLY_RESERVED;
        } else if (hasBackorder) {
            return SOStatus.BACKORDERED;
        } else {
            return SOStatus.RESERVED;
        }
    }
}
