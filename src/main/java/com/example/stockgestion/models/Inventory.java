package com.example.stockgestion.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "inventories", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"product_id", "warehouse_id"})
})
@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    @NotNull
    private UUID id;

    @NotNull
    @Column(name = "qty_on_hand", nullable = false)
    private long qtyOnHand;

    @NotNull
    @Column(name = "qty_reserved", nullable = false)
    private long qtyReserved;

    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name = "product_id",nullable = false)
    @NotNull
    private Product product;

    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    @NotNull
    private WareHouse warehouse;
}
