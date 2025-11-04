package com.example.stockgestion.services;

import com.example.stockgestion.Dto.request.PurchaseOrderRequestDto;
import com.example.stockgestion.Dto.response.PurchaseOrderResponseDto;
import com.example.stockgestion.exception.ResourceNotFoundException;
import com.example.stockgestion.models.Product;
import com.example.stockgestion.models.PurchaseOrder;
import com.example.stockgestion.models.PurchaseOrderLine;
import com.example.stockgestion.models.Supplier;
import com.example.stockgestion.models.enums.POStatus;
import com.example.stockgestion.repositories.InventoryRepository;
import com.example.stockgestion.repositories.ProductRepository;
import com.example.stockgestion.repositories.PurchaseOrderRepository;
import com.example.stockgestion.repositories.SupplierRepository;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class PurchaseOrderService {
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final InventoryRepository inventoryRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    @Transactional
    public PurchaseOrderResponseDto createPurchaseOrder(PurchaseOrderRequestDto purchaseOrderRequestDto) {
        Supplier supplier = supplierRepository.findById(purchaseOrderRequestDto.getSupplierId()).orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));
        PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.setSupplier(supplier);
        purchaseOrder.setStatus(POStatus.APPROVED);
        List<PurchaseOrderLine> purchaseOrdersLines = new ArrayList<>();
        purchaseOrderRequestDto.getLines().forEach(line -> {
            Product product = productRepository.findById(line.getProductId()).orElseThrow(()-> new ResourceNotFoundException("Product not found"));
            PurchaseOrderLine purchaseOrderLine = new PurchaseOrderLine();
            purchaseOrderLine.setProduct(product);
            purchaseOrderLine.setQuantity(line.getQuantity());
            purchaseOrderLine.setUnitPrice(line.getUnitPrice());
            purchaseOrderLine.setPurchaseOrder(purchaseOrder);
            purchaseOrdersLines.add(purchaseOrderLine);
        });
        purchaseOrder.setLines(purchaseOrdersLines);
        PurchaseOrder savedOrder = purchaseOrderRepository.save(purchaseOrder);
        return modelMapper.map(savedOrder, PurchaseOrderResponseDto.class);
    }

}
