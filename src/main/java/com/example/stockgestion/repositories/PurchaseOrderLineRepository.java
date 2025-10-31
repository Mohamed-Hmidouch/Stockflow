package com.example.stockgestion.repositories;

import com.example.stockgestion.models.PurchaseOrderLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PurchaseOrderLineRepository extends JpaRepository<PurchaseOrderLine, UUID> {
}
