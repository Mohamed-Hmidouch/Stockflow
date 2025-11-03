package com.example.stockgestion.models;

import com.example.stockgestion.models.enums.SOStatus; // Importez le nouveau enum
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "sales_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    // ----- RELATION VERS LE CLIENT -----
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    // ----- STATUT DE LA COMMANDE -----
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SOStatus status;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(precision = 19, scale = 2)
    private BigDecimal totalPrice;

    // ----- RELATION VERS LES LIGNES DE COMMANDE -----
    @OneToMany(
            mappedBy = "salesOrder", // "salesOrder" = champ dans SalesOrderLine
            cascade = CascadeType.ALL, // Si on supprime la commande, on supprime les lignes
            orphanRemoval = true
    )
    private List<SalesOrderLine> lines;

    // ----- RELATION VERS L'EXPÉDITION -----
    // Une commande peut avoir plusieurs expéditions (ex: backorder)
    @OneToMany(
            mappedBy = "salesOrder",
            fetch = FetchType.LAZY
    )
    private List<Shipment> shipments;
}