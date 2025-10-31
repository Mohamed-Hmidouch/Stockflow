package com.example.stockgestion.repositories;

import com.example.stockgestion.models.SalesOrderLine;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SalesOrderLineRepository extends JpaRepository<SalesOrderLine, UUID> {
}
