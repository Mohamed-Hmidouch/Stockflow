package com.example.stockgestion.models;

import com.example.stockgestion.models.enums.MovementType; // Importez le enum
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "inventory_movements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    // ----- QUEL PRODUIT A BOUGÉ ? -----
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // ----- DANS QUEL ENTREPÔT ? -----
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private WareHouse warehouse; // (ou WareHouse)

    // ----- QUEL TYPE DE MOUVEMENT ? -----
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MovementType type;

    /**
     * La quantité de ce mouvement.
     * Positive pour INBOUND/ADJUSTMENT positif.
     * Négative pour OUTBOUND/ADJUSTMENT négatif.
     */
    @Column(nullable = false)
    private long quantity;

    @NotNull
    @Column(nullable = false)
    private Instant occurredAt; // Quand le mouvement a-t-il eu lieu

    // Référence au document (ex: ID du SalesOrder ou PurchaseOrder)
    @Column
    private String referenceDoc;
}