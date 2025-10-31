package com.example.stockgestion.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "carriers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Carrier {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @NotBlank
    @Column(nullable = false, unique = true)
    private String name; // Ex: "DHL", "FedEx", "La Poste"

    @Column(nullable = false)
    private boolean active = true;

    // ----- RELATION INVERSE VERS LES EXPÉDITIONS -----
    @OneToMany(
            mappedBy = "carrier", // "carrier" = nom du champ dans l'entité Shipment
            fetch = FetchType.LAZY
    )
    private List<Shipment> shipments;
}