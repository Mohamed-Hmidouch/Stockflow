package com.example.stockgestion.services.helpers;

import com.example.stockgestion.models.SalesOrderLine;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

/**
 * Résultat d'une opération de réservation de stock
 */
@Getter
@AllArgsConstructor
public class ReservationResult {
    private final List<SalesOrderLine> lines;
    private final boolean hasReserved;
    private final boolean hasBackorder;
}
