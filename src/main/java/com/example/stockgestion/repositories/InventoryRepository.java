package com.example.stockgestion.repositories;

import com.example.stockgestion.models.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository extends JpaRepository<Inventory, UUID> {
	boolean existsByProduct_IdAndWarehouse_Id(UUID productId, UUID warehouseId);
	List<Inventory> findByProduct_Id(UUID productId);
	Optional<Inventory> findByProduct_IdAndWarehouse_Id(UUID productId, UUID warehouseId);
}