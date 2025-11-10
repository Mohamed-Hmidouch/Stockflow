package com.example.stockgestion.repositories;

import com.example.stockgestion.models.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, UUID> {

    void findByClientId(UUID id);
    // TODO: Fix this query - productId is not a direct property of SalesOrder
    // List<SalesOrder> findByProductIdAndQtyBackorderedGreaterThanOrderByCreatedAt(UUID productId, long qtyBackordered);
    
    // TODO: Cette méthode est incorrecte - SalesOrder n'a pas de propriété 'product' directement
    // int countByProduct_SkuAndOrder_StatusIn(String sku, List<String> statuses);
}