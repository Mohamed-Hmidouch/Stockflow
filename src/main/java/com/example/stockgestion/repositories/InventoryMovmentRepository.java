package com.example.stockgestion.repositories;

import com.example.stockgestion.models.InventoryMovement;
import com.example.stockgestion.models.enums.MovementType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.UUID;

public interface InventoryMovmentRepository extends JpaRepository<InventoryMovement, UUID> {
    Page<InventoryMovement> findByProduct_Id(UUID productId, Pageable pageable);
    Page<InventoryMovement> findByWarehouse_Id(UUID warehouseId, Pageable pageable);
    Page<InventoryMovement> findByType(MovementType type, Pageable pageable);
    Page<InventoryMovement> findByOccurredAtBetween(Instant from, Instant to, Pageable pageable);
    Page<InventoryMovement> findByProduct_IdAndWarehouse_Id(UUID productId, UUID warehouseId, Pageable pageable);
    Page<InventoryMovement> findByProduct_IdAndType(UUID productId, MovementType type, Pageable pageable);
    Page<InventoryMovement> findByWarehouse_IdAndType(UUID warehouseId, MovementType type, Pageable pageable);
    Page<InventoryMovement> findByProduct_IdAndWarehouse_IdAndType(UUID productId, UUID warehouseId, MovementType type, Pageable pageable);
}
