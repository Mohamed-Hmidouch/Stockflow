package com.example.stockgestion.repositories;

import com.example.stockgestion.models.SalesOrderLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SalesOrderLineRepository extends JpaRepository<SalesOrderLine, UUID> {
    List<SalesOrderLine> findByProductIdAndQtyBackorderedGreaterThanOrderByCreatedAtAsc(UUID productId, long qtyBackordered);
    List<SalesOrderLine> findByProductId(UUID productId);
}