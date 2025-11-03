package com.example.stockgestion.services.helpers;

import com.example.stockgestion.models.Client;
import com.example.stockgestion.models.SalesOrder;
import com.example.stockgestion.models.SalesOrderLine;
import com.example.stockgestion.models.enums.SOStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Helper pour construire et finaliser les commandes
 */
@Component
public class SalesOrderBuilder {

    /**
     * Initialise une nouvelle commande
     */
    public SalesOrder initialize(Client client) {
        return SalesOrder.builder()
            .client(client)
            .createdAt(Instant.now())
            .status(SOStatus.CREATED)
            .build();
    }

    /**
     * Finalise une commande (set status, lines, totalPrice)
     */
    public void finalize(SalesOrder order, List<SalesOrderLine> lines, BigDecimal totalPrice,
                        SOStatus status) {
        order.setStatus(status);
        order.setLines(lines);
        order.setTotalPrice(totalPrice);
    }
}
