package com.example.stockgestion.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

// ... autres imports ...

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "products")
public class Product {

    // Comment annoter l'ID ?
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id",nullable = false)
    private UUID id;

    // Comment annoter sku pour qu'il soit unique et non nul ?
    @NotNull
    @Column(name = "sku" ,unique = true)
    private String sku;

    // Comment annoter name pour qu'il soit non nul ?
    @NotNull
    @Column(name = "name",nullable = false)
    private String name;

    // category peut rester simple
    @Column(name = "category")
    private String category;

    // Comment s'assurer que 'active' est non nul ?
    @NotNull
    @Column(name = "active",nullable = false)
    private Boolean active;

    @Column(name = "price", precision = 19, scale = 2)
    private BigDecimal price;

    @OneToMany(mappedBy = "product")
    private List<Inventory> inventoriesProduct;
}