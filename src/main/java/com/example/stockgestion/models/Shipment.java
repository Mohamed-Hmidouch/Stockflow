package com.example.stockgestion.models;

import com.example.stockgestion.models.enums.ShipmentStatus; // Importez le enum
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "shipments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Shipment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    // ----- RELATION VERS LA COMMANDE CLIENT -----
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_order_id", nullable = false)
    private SalesOrder salesOrder;

    // ----- RELATION VERS LE TRANSPORTEUR -----
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "carrier_id", nullable = false)
    private Carrier carrier;

    // ----- STATUT DE L'EXPÉDITION -----
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ShipmentStatus status;

    @Column(unique = true) // Le numéro de suivi doit être unique
    private String trackingNumber;

    private Instant shippedAt; // Date de départ
    private Instant deliveredAt; // Date de livraison
}