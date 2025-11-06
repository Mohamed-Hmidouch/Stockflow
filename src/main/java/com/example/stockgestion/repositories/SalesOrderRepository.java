package com.example.stockgestion.repositories;

import com.example.stockgestion.models.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, UUID> {

    void findByClientId(UUID id);
    // TODO: Fix this query - productId is not a direct property of SalesOrder
    // List<SalesOrder> findByProductIdAndQtyBackorderedGreaterThanOrderByCreatedAt(UUID productId, long qtyBackordered);
}
