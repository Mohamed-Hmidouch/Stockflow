package com.example.stockgestion.repositories;

import com.example.stockgestion.models.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ShipmentRepository extends JpaRepository<Shipment, UUID> {
}
