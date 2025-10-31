package com.example.stockgestion.repositories;

import com.example.stockgestion.models.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InventoryRepository extends JpaRepository<Inventory, UUID> {
}
