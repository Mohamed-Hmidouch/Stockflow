package com.example.stockgestion.repositories;

import com.example.stockgestion.models.Carrier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CarrierRepository extends JpaRepository<Carrier, UUID> {
}
