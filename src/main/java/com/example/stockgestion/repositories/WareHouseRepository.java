package com.example.stockgestion.repositories;

import com.example.stockgestion.models.WareHouse;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface WareHouseRepository extends JpaRepository<WareHouse, UUID> {

    public Boolean existsByCode(String code);
}
