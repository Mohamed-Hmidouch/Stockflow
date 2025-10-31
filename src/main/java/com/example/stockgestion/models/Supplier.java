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
@Table(name = "suppliers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @NotBlank
    @Column(nullable = false, unique = true) // Le nom du fournisseur doit être unique
    private String name;

    @Column // Le contact peut être optionnel
    private String contact;

    // ----- RELATION INVERSE VERS LES BONS DE COMMANDE -----
    /**
     * Représente la liste de tous les bons de commande
     * émis à ce fournisseur.
     */
    @OneToMany(
            mappedBy = "supplier", // "supplier" = nom du champ dans l'entité PurchaseOrder
            fetch = FetchType.LAZY
    )
    private List<PurchaseOrder> purchaseOrders;
}