package com.example.stockgestion.repositories;

import com.example.stockgestion.models.Shipment;
import com.example.stockgestion.models.enums.ShipmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface ShipmentRepository extends JpaRepository<Shipment, UUID> {
    
    /**
     * Trouver les expéditions d'une commande
     */
    List<Shipment> findBySalesOrderId(UUID salesOrderId);
    
    /**
     * Trouver les expéditions par statut
     */
    List<Shipment> findByStatus(ShipmentStatus status);
    
    /**
     * Compter les expéditions planifiées pour un créneau donné
     * Utilisé pour vérifier la capacité du slot
     */
    @Query("SELECT COUNT(s) FROM Shipment s WHERE s.plannedDepartureDate BETWEEN :startDate AND :endDate")
    long countByPlannedDepartureDateBetween(
            @Param("startDate") Instant startDate, 
            @Param("endDate") Instant endDate
    );
}
