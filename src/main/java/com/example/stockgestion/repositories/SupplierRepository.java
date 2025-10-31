package com.example.stockgestion.repositories;

import com.example.stockgestion.models.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SupplierRepository extends JpaRepository<Supplier,UUID>{
}
