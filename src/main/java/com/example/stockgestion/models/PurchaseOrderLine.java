package com.example.stockgestion.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal; // Pour les prix !
import java.util.UUID;

@Entity
@Table(name = "purchase_order_lines")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderLine {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    // ----- RELATION VERS L'EN-TÊTE DE COMMANDE -----
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false) // Côté propriétaire
    private PurchaseOrder purchaseOrder;

    // ----- RELATION VERS LE PRODUIT COMMANDÉ -----
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Positive // Une quantité doit être > 0
    @Column(nullable = false)
    private long quantity;

    // (Note: J'ai ajouté unitPrice, présent sur votre diagramme UML)
    @Column(precision = 10, scale = 2) // Important pour l'argent
    private BigDecimal unitPrice;
}