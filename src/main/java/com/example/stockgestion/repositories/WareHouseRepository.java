package com.example.stockgestion.repositories;

import com.example.stockgestion.models.WareHouse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface WareHouseRepository extends JpaRepository<WareHouse, UUID> {

    public Boolean existsByCode(String code);
}
