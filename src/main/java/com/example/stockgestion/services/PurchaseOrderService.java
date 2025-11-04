package com.example.stockgestion.services;

import com.example.stockgestion.Dto.request.PurchaseOrderRequestDto;
import com.example.stockgestion.Dto.request.ReceiveRequestDto;
import com.example.stockgestion.Dto.response.PurchaseOrderResponseDto;
import com.example.stockgestion.exception.ResourceNotFoundException;
import com.example.stockgestion.models.*;
import com.example.stockgestion.models.enums.MovementType;
import com.example.stockgestion.models.enums.POStatus;
import com.example.stockgestion.repositories.*;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class PurchaseOrderService {
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderLineRepository purchaseOrderLineRepository;
    private final InventoryRepository inventoryRepository;
    private final SupplierRepository supplierRepository;
    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;
    private final WareHouseRepository wareHouseRepository;
    private final InventoryMovmentRepository inventoryMovmentRepository;

    @Transactional
    public PurchaseOrderResponseDto createPurchaseOrder(PurchaseOrderRequestDto purchaseOrderRequestDto) {
        Supplier supplier = supplierRepository.findById(purchaseOrderRequestDto.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Supplier not found"));
        PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.setSupplier(supplier);
        purchaseOrder.setStatus(POStatus.APPROVED);
        List<PurchaseOrderLine> purchaseOrdersLines = new ArrayList<>();
        purchaseOrderRequestDto.getLines().forEach(line -> {
            Product product = productRepository.findById(line.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
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

    public PurchaseOrderResponseDto receptionOrder(UUID orderId, ReceiveRequestDto requestDto) {
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase Order not found"));
        WareHouse wareHouse = wareHouseRepository.findById(requestDto.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found"));
        if (purchaseOrder.getStatus() == POStatus.RECEIVED || purchaseOrder.getStatus() == POStatus.CANCELED)
            throw new ResourceNotFoundException("Purchase Order Status not found");
        List<Inventory> inventoriesToUpdate = new ArrayList<>();
        List<InventoryMovement> movmentsToCreate = new ArrayList<>();
        requestDto.getReceivedLineDto().forEach(line -> {
            UUID productUuid = UUID.fromString(line.getProductId().toString());
            Product product = productRepository.findById(productUuid)
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
            Inventory inventory = inventoryRepository
                    .findByProduct_IdAndWarehouse_Id(productUuid, requestDto.getWarehouseId()).stream().findFirst()
                    .orElseGet(() -> Inventory.builder()
                            .product(product)
                            .warehouse(wareHouse)
                            .qtyOnHand(0)
                            .qtyReserved(0)
                            .build());
            long qtyReceived = line.getQuantityReceived();
            inventory.setQtyOnHand(inventory.getQtyOnHand() + qtyReceived);
            inventoriesToUpdate.add(inventory);
            InventoryMovement inventoryMovement = new InventoryMovement();
            inventoryMovement.setOccurredAt(Instant.now());
            inventoryMovement.setProduct(product);
            inventoryMovement.setWarehouse(wareHouse);
            inventoryMovement.setType(MovementType.INBOUND);
            inventoryMovement.setQuantity(qtyReceived);
            movmentsToCreate.add(inventoryMovement);
        });
        List<PurchaseOrderLine> poLineToUpdate = new ArrayList<>();
        requestDto.getReceivedLineDto().forEach(line -> {
            UUID productId = UUID.fromString(line.getProductId().toString());
            PurchaseOrderLine poLine = purchaseOrder.getLines().stream()
                    .filter(l -> l.getProduct().getId().equals(productId)).findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("line not found"));
            poLine.setQtyReceived(poLine.getQtyReceived() + line.getQuantityReceived());
            poLineToUpdate.add(poLine);
        });
        purchaseOrderLineRepository.saveAll(poLineToUpdate);
        inventoryRepository.saveAll(inventoriesToUpdate);
        inventoryMovmentRepository.saveAll(movmentsToCreate);
        boolean allLinesCompleted = true;
        for (PurchaseOrderLine l : purchaseOrder.getLines()) {
            if (l.getQtyReceived() < l.getQuantity()) {
                allLinesCompleted = false;
                break;
            }
        }
        if (allLinesCompleted)
            purchaseOrder.setStatus(POStatus.RECEIVED);
        else
            purchaseOrder.setStatus(POStatus.PARTIALLY_RECEIVED);
        PurchaseOrder orderSaved = purchaseOrderRepository.save(purchaseOrder);
        return modelMapper.map(orderSaved, PurchaseOrderResponseDto.class);
    }

    public List<PurchaseOrderResponseDto> getAllPurchaseOrders() {
        List<PurchaseOrder> purchaseOrders = purchaseOrderRepository.findAll();
        return purchaseOrders.stream()
                .map(order -> modelMapper.map(order, PurchaseOrderResponseDto.class))
                .toList();
    }
}
