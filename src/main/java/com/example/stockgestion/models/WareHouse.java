package com.example.stockgestion.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "warehouses")
public class WareHouse {
    @Id
    @NotNull
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(unique = true, nullable = false)
    private UUID id;

    @Column(name = "code", nullable = false,unique = true)
    @NotBlank
    private String code;

    @NotBlank
    @Column(name = "name",nullable = false)
    private String name;

    @OneToMany(mappedBy = "warehouse",fetch =  FetchType.LAZY)
    private List<Inventory> inventoriesLines;
}