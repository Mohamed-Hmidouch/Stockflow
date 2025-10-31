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
@Table(name = "clients")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @NotBlank
    @Column(nullable = false, unique = true) // Le nom du client doit être unique
    private String name;

    // ----- RELATION INVERSE VERS LES COMMANDES CLIENTS -----
    /**
     * Représente la liste de toutes les commandes
     * passées par ce client.
     */
    @OneToMany(
            mappedBy = "client", // "client" = nom du champ dans l'entité SalesOrder
            fetch = FetchType.LAZY
    )
    private List<SalesOrder> salesOrders;
}