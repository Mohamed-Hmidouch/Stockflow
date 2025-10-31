package com.example.stockgestion.repositories;

import com.example.stockgestion.models.Product;
import org.hibernate.annotations.processing.SQL;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    public Boolean existsBySku(String sku);
}
