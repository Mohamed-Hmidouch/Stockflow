package com.example.stockgestion.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.stockgestion.Dto.request.PurchaseOrderRequestDto;
import com.example.stockgestion.Dto.response.PurchaseOrderResponseDto;
import com.example.stockgestion.exception.ResourceNotFoundException;
import com.example.stockgestion.models.InventoryMovement;
import com.example.stockgestion.models.PurchaseOrder;
import com.example.stockgestion.models.PurchaseOrderLine;
import com.example.stockgestion.models.enums.MovementType;
import com.example.stockgestion.models.enums.POStatus;
import com.example.stockgestion.models.enums.SOStatus;
import com.example.stockgestion.repositories.InventoryMovmentRepository;
import com.example.stockgestion.repositories.PurchaseOrderLineRepository;
import com.example.stockgestion.repositories.PurchaseOrderRepository;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Service
@AllArgsConstructor
@NoArgsConstructor
public class stock_miseSit {
    private  PurchaseOrderLineRepository purchaseOrderLineRepository;
    private  PurchaseOrderRepository purchaseOrderRepository;
    private  InventoryMovmentRepository inventoryMovmentRepository;

    public PurchaseOrderResponseDto receivePurchaseOrder(UUID purchaseOrderId){
        Optional<PurchaseOrder> purchaseOrder = purchaseOrderRepository.findById(purchaseOrderId).stream().filter(p -> p.getStatus() == POStatus.APPROVED).findFirst();
        if (purchaseOrder == null)
            throw new ResourceNotFoundException("cette purshaseorder est pas approved encore" + purchaseOrderId);

            List<PurchaseOrderLine> linesToUpdate = new ArrayList<>();
            
            for(PurchaseOrderLine pOrderline : purchaseOrder.get().getLines())
            {
                pOrderline.setQuantity(10);
                InventoryMovement movement = new InventoryMovement();
                movement.setProduct(pOrderline.getProduct());
                movement.setReferenceDoc(purchaseOrderId.toString());
                movement.setType(MovementType.INBOUND);
            }
            return null;
    }
}