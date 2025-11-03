package com.example.stockgestion.repositories;

import com.example.stockgestion.models.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, UUID> {
    void findbyClientId(UUID id);
}
