package com.example.stockgestion.repositories;

import com.example.stockgestion.models.InventoryMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface InventoryMovmentRepository extends JpaRepository<InventoryMovement, UUID> {
}
