package com.example.stockgestion.models;

import com.example.stockgestion.models.enums.POStatus; // Importez l'enum
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "purchase_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    // ----- RELATION VERS LE FOURNISSEUR -----
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false) // Côté propriétaire
    private Supplier supplier;

    // ----- STATUT DE LA COMMANDE -----
    @NotNull
    @Enumerated(EnumType.STRING) // Stocke "DRAFT", "APPROVED" etc. en BDD
    @Column(nullable = false, length = 20)
    private POStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    // ----- RELATION VERS LES LIGNES DE COMMANDE -----
    @OneToMany(
            mappedBy = "purchaseOrder", // "purchaseOrder" = champ dans PurchaseOrderLine
            cascade = CascadeType.ALL, // On explique 'cascade' après
            orphanRemoval = true       // Et 'orphanRemoval'
    )
    private List<PurchaseOrderLine> lines;
}