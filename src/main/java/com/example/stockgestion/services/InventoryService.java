package com.example.stockgestion.services;

import com.example.stockgestion.Dto.request.InventoryRequestDto;
import com.example.stockgestion.Dto.response.InventoryResponseDto;
import com.example.stockgestion.exception.ConflictException;
import com.example.stockgestion.exception.ResourceNotFoundException;
import com.example.stockgestion.models.Inventory;
import com.example.stockgestion.models.Product;
import com.example.stockgestion.models.WareHouse;
import com.example.stockgestion.repositories.InventoryRepository;
import com.example.stockgestion.repositories.InventoryMovmentRepository;
import com.example.stockgestion.Dto.request.InventoryMovementRequestDto;
import com.example.stockgestion.Dto.response.InventoryMovementResponseDto;
import com.example.stockgestion.models.InventoryMovement;
import com.example.stockgestion.exception.BusinessRuleException;
import com.example.stockgestion.repositories.ProductRepository;
import com.example.stockgestion.repositories.WareHouseRepository;
import com.example.stockgestion.repositories.SalesOrderRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class InventoryService {
    private final InventoryRepository inventoryRepository;
    private final ProductRepository productRepository;
    private final WareHouseRepository wareHouseRepository;
    private final InventoryMovmentRepository inventoryMovmentRepository;
    private final SalesOrderRepository salesOrderRepository;

    @Transactional
    public InventoryResponseDto createInventory(InventoryRequestDto dto) {
        // ensure product exists
        Product product = productRepository.findById(dto.getProductId()).orElseThrow(() ->
                new ResourceNotFoundException("Product", "id", dto.getProductId())
        );

        // ensure warehouse exists
        WareHouse warehouse = wareHouseRepository.findById(dto.getWarehouseId()).orElseThrow(() ->
                new ResourceNotFoundException("WareHouse", "id", dto.getWarehouseId())
        );

        // check uniqueness (product + warehouse)
        if (inventoryRepository.existsByProduct_IdAndWarehouse_Id(dto.getProductId(), dto.getWarehouseId())) {
            throw new ConflictException("Inventory already exists for product '" + dto.getProductId() + "' in warehouse '" + dto.getWarehouseId() + "'.");
        }

        Inventory inventory = new Inventory();
        inventory.setQtyOnHand(dto.getQtyOnHand());
        inventory.setQtyReserved(dto.getQtyReserved());
        inventory.setProduct(product);
        inventory.setWarehouse(warehouse);

        Inventory saved = inventoryRepository.save(inventory);
        return new InventoryResponseDto(saved);
    }

    @Transactional(readOnly = true)
    public InventoryResponseDto getInventoryById(UUID id) {
        Inventory inventory = inventoryRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Inventory", "id", id)
        );
        return new InventoryResponseDto(inventory);
    }

    @Transactional(readOnly = true)
    public List<InventoryResponseDto> getAllInventories() {
        List<Inventory> list = inventoryRepository.findAll();
        return list.stream().map(InventoryResponseDto::new).toList();
    }

    @Transactional(readOnly = true)
    public List<InventoryResponseDto> getByProductId(UUID productId) {
        List<Inventory> list = inventoryRepository.findByProduct_Id(productId);
        return list.stream().map(InventoryResponseDto::new).toList();
    }

    @Transactional(readOnly = true)
    public List<InventoryResponseDto> getByWarehouseId(UUID warehouseId) {
        List<Inventory> list = inventoryRepository.findByWarehouse_Id(warehouseId);
        return list.stream().map(InventoryResponseDto::new).toList();
    }

    @Transactional
    public InventoryMovementResponseDto createMovement(InventoryMovementRequestDto dto) {
        // validate product and warehouse
        Product product = productRepository.findById(dto.getProductId()).orElseThrow(() ->
                new ResourceNotFoundException("Product", "id", dto.getProductId())
        );
        WareHouse warehouse = wareHouseRepository.findById(dto.getWarehouseId()).orElseThrow(() ->
                new ResourceNotFoundException("WareHouse", "id", dto.getWarehouseId())
        );

        // find or initialize inventory
        Inventory inventory = inventoryRepository.findByProduct_IdAndWarehouse_Id(dto.getProductId(), dto.getWarehouseId())
                .orElseGet(() -> {
                    Inventory i = new Inventory();
                    i.setProduct(product);
                    i.setWarehouse(warehouse);
                    i.setQtyOnHand(0);
                    i.setQtyReserved(0);
                    return i;
                });

        long qty = dto.getQuantity();
        switch (dto.getType()) {
            case INBOUND:
                inventory.setQtyOnHand(inventory.getQtyOnHand() + qty);
                break;
            case OUTBOUND:
                if (inventory.getQtyOnHand() < qty) {
                    throw new BusinessRuleException("Stock insuffisant pour sortie: demandé=" + qty + ", disponible=" + inventory.getQtyOnHand());
                }
                inventory.setQtyOnHand(inventory.getQtyOnHand() - qty);
                break;
            case ADJUSTMENT:
                long newQty = inventory.getQtyOnHand() + qty;
                if (newQty < 0) {
                    throw new BusinessRuleException("L'ajustement mène à un stock négatif: " + newQty);
                }
                inventory.setQtyOnHand(newQty);
                break;
        }

        inventoryRepository.save(inventory);

        InventoryMovement movement = new InventoryMovement();
        movement.setProduct(product);
        movement.setWarehouse(warehouse);
        movement.setType(dto.getType());
        movement.setQuantity(dto.getQuantity());
        movement.setOccurredAt(dto.getOccurredAt() != null ? dto.getOccurredAt() : java.time.Instant.now());
        movement.setReferenceDoc(dto.getReferenceDoc());

        InventoryMovement savedMovement = inventoryMovmentRepository.save(movement);

        return new InventoryMovementResponseDto(savedMovement);
    }

    @Transactional
    public InventoryResponseDto updateInventory(UUID id, InventoryRequestDto dto) {
        Inventory existing = inventoryRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Inventory", "id", id)
        );

        // If product or warehouse changed, ensure they exist and uniqueness
        if (dto.getProductId() != null && !dto.getProductId().equals(existing.getProduct().getId())) {
            Product product = productRepository.findById(dto.getProductId()).orElseThrow(() ->
                    new ResourceNotFoundException("Product", "id", dto.getProductId())
            );
            if (inventoryRepository.existsByProduct_IdAndWarehouse_Id(dto.getProductId(), existing.getWarehouse().getId())) {
                throw new ConflictException("Another inventory already exists for product '" + dto.getProductId() + "' in warehouse '" + existing.getWarehouse().getId() + "'.");
            }
            existing.setProduct(product);
        }

        if (dto.getWarehouseId() != null && !dto.getWarehouseId().equals(existing.getWarehouse().getId())) {
            WareHouse warehouse = wareHouseRepository.findById(dto.getWarehouseId()).orElseThrow(() ->
                    new ResourceNotFoundException("WareHouse", "id", dto.getWarehouseId())
            );
            if (inventoryRepository.existsByProduct_IdAndWarehouse_Id(existing.getProduct().getId(), dto.getWarehouseId())) {
                throw new ConflictException("Another inventory already exists for product '" + existing.getProduct().getId() + "' in warehouse '" + dto.getWarehouseId() + "'.");
            }
            existing.setWarehouse(warehouse);
        }

        // update quantities
        existing.setQtyOnHand(dto.getQtyOnHand());
        existing.setQtyReserved(dto.getQtyReserved());

        Inventory saved = inventoryRepository.save(existing);
        return new InventoryResponseDto(saved);
    }

    @Transactional
    public void deleteInventory(UUID id) {
        Inventory existing = inventoryRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Inventory", "id", id)
        );
        inventoryRepository.delete(existing);
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<com.example.stockgestion.Dto.response.InventoryMovementResponseDto> listMovements(
            java.util.UUID productId,
            java.util.UUID warehouseId,
            com.example.stockgestion.models.enums.MovementType type,
            java.time.Instant from,
            java.time.Instant to,
            org.springframework.data.domain.Pageable pageable
    ) {
        org.springframework.data.domain.Page<com.example.stockgestion.models.InventoryMovement> page;
        boolean hasProduct = productId != null;
        boolean hasWarehouse = warehouseId != null;
        boolean hasType = type != null;
        boolean hasRange = from != null && to != null;

        if (hasRange) {
            // range-only filter
            page = inventoryMovmentRepository.findByOccurredAtBetween(from, to, pageable);
        } else if (hasProduct && hasWarehouse && hasType) {
            page = inventoryMovmentRepository.findByProduct_IdAndWarehouse_IdAndType(productId, warehouseId, type, pageable);
        } else if (hasProduct && hasWarehouse) {
            page = inventoryMovmentRepository.findByProduct_IdAndWarehouse_Id(productId, warehouseId, pageable);
        } else if (hasProduct && hasType) {
            page = inventoryMovmentRepository.findByProduct_IdAndType(productId, type, pageable);
        } else if (hasWarehouse && hasType) {
            page = inventoryMovmentRepository.findByWarehouse_IdAndType(warehouseId, type, pageable);
        } else if (hasProduct) {
            page = inventoryMovmentRepository.findByProduct_Id(productId, pageable);
        } else if (hasWarehouse) {
            page = inventoryMovmentRepository.findByWarehouse_Id(warehouseId, pageable);
        } else if (hasType) {
            page = inventoryMovmentRepository.findByType(type, pageable);
        } else {
            page = inventoryMovmentRepository.findAll(pageable);
        }

        return page.map(com.example.stockgestion.Dto.response.InventoryMovementResponseDto::new);
    }

    @Transactional
    public void reserve(com.example.stockgestion.Dto.request.ReservationRequestDto dto) {
        com.example.stockgestion.models.SalesOrder order = salesOrderRepository.findById(dto.getSalesOrderId()).orElseThrow(() ->
                new com.example.stockgestion.exception.ResourceNotFoundException("SalesOrder", "id", dto.getSalesOrderId())
        );
        if (order.getStatus() != com.example.stockgestion.models.enums.SOStatus.CREATED) {
            throw new com.example.stockgestion.exception.BusinessRuleException("La commande doit être au statut CREATED pour réserver");
        }
        for (com.example.stockgestion.models.SalesOrderLine line : order.getLines()) {
            java.util.UUID productId = line.getProduct().getId();
            java.util.UUID warehouseId = line.getWarehouse().getId();
            long requested = line.getQuantity();
            com.example.stockgestion.models.Inventory inv = inventoryRepository.findByProduct_IdAndWarehouse_Id(productId, warehouseId)
                    .orElseThrow(() -> new com.example.stockgestion.exception.BusinessRuleException("Inventaire introuvable pour le produit et l'entrepôt"));
            long dispo = inv.getQtyOnHand() - inv.getQtyReserved();
            if (dispo < requested) {
                throw new com.example.stockgestion.exception.BusinessRuleException("Stock insuffisant: demandé=" + requested + ", dispo=" + dispo);
            }
            inv.setQtyReserved(inv.getQtyReserved() + requested);
            inventoryRepository.save(inv);
        }
        order.setStatus(com.example.stockgestion.models.enums.SOStatus.RESERVED);
        salesOrderRepository.save(order);
    }

    @Transactional
    public void outbound(com.example.stockgestion.Dto.request.OutboundRequestDto dto) {
        com.example.stockgestion.models.SalesOrder order = salesOrderRepository.findById(dto.getSalesOrderId()).orElseThrow(() ->
                new com.example.stockgestion.exception.ResourceNotFoundException("SalesOrder", "id", dto.getSalesOrderId())
        );
        if (order.getStatus() != com.example.stockgestion.models.enums.SOStatus.RESERVED) {
            throw new com.example.stockgestion.exception.BusinessRuleException("La commande doit être au statut RESERVED pour expédier");
        }
        for (com.example.stockgestion.models.SalesOrderLine line : order.getLines()) {
            java.util.UUID productId = line.getProduct().getId();
            java.util.UUID warehouseId = line.getWarehouse().getId();
            long qty = line.getQuantity();
            com.example.stockgestion.models.Inventory inv = inventoryRepository.findByProduct_IdAndWarehouse_Id(productId, warehouseId)
                    .orElseThrow(() -> new com.example.stockgestion.exception.BusinessRuleException("Inventaire introuvable pour le produit et l'entrepôt"));
            if (inv.getQtyReserved() < qty) {
                throw new com.example.stockgestion.exception.BusinessRuleException("Réservation insuffisante pour expédier");
            }
            inv.setQtyOnHand(inv.getQtyOnHand() - qty);
            inv.setQtyReserved(inv.getQtyReserved() - qty);
            inventoryRepository.save(inv);

            com.example.stockgestion.models.InventoryMovement movement = new com.example.stockgestion.models.InventoryMovement();
            movement.setProduct(line.getProduct());
            movement.setWarehouse(line.getWarehouse());
            movement.setType(com.example.stockgestion.models.enums.MovementType.OUTBOUND);
            movement.setQuantity(-qty);
            movement.setOccurredAt(java.time.Instant.now());
            movement.setReferenceDoc(order.getId().toString());
            inventoryMovmentRepository.save(movement);
        }
        order.setStatus(com.example.stockgestion.models.enums.SOStatus.SHIPPED);
        salesOrderRepository.save(order);
    }

    @Transactional
    public com.example.stockgestion.Dto.response.InventoryMovementResponseDto adjust(com.example.stockgestion.Dto.request.AdjustmentRequestDto dto) {
        com.example.stockgestion.models.Product product = productRepository.findById(dto.getProductId()).orElseThrow(() ->
                new com.example.stockgestion.exception.ResourceNotFoundException("Product", "id", dto.getProductId())
        );
        com.example.stockgestion.models.WareHouse warehouse = wareHouseRepository.findById(dto.getWarehouseId()).orElseThrow(() ->
                new com.example.stockgestion.exception.ResourceNotFoundException("WareHouse", "id", dto.getWarehouseId())
        );
        com.example.stockgestion.models.Inventory inv = inventoryRepository.findByProduct_IdAndWarehouse_Id(dto.getProductId(), dto.getWarehouseId())
                .orElseGet(() -> {
                    com.example.stockgestion.models.Inventory i = new com.example.stockgestion.models.Inventory();
                    i.setProduct(product);
                    i.setWarehouse(warehouse);
                    i.setQtyOnHand(0);
                    i.setQtyReserved(0);
                    return i;
                });
        long quantity = dto.getQuantity();
        if (quantity < 0) {
            long need = Math.abs(quantity);
            long dispo = inv.getQtyOnHand() - inv.getQtyReserved();
            if (dispo < need) {
                throw new com.example.stockgestion.exception.BusinessRuleException("Ajustement négatif impossible: dispo=" + dispo + ", demandé=" + need);
            }
        }
        inv.setQtyOnHand(inv.getQtyOnHand() + quantity);
        inventoryRepository.save(inv);

        com.example.stockgestion.models.InventoryMovement movement = new com.example.stockgestion.models.InventoryMovement();
        movement.setProduct(product);
        movement.setWarehouse(warehouse);
        movement.setType(com.example.stockgestion.models.enums.MovementType.ADJUSTMENT);
        movement.setQuantity(quantity);
        movement.setOccurredAt(dto.getOccurredAt() != null ? dto.getOccurredAt() : java.time.Instant.now());
        movement.setReferenceDoc(dto.getReferenceDoc());
        com.example.stockgestion.models.InventoryMovement savedMovement = inventoryMovmentRepository.save(movement);

        return new com.example.stockgestion.Dto.response.InventoryMovementResponseDto(savedMovement);
    }

    @Transactional
    public void releaseReservation(com.example.stockgestion.Dto.request.ReservationRequestDto dto) {
        com.example.stockgestion.models.SalesOrder order = salesOrderRepository.findById(dto.getSalesOrderId()).orElseThrow(() ->
                new com.example.stockgestion.exception.ResourceNotFoundException("SalesOrder", "id", dto.getSalesOrderId())
        );
        if (order.getStatus() != com.example.stockgestion.models.enums.SOStatus.RESERVED) {
            throw new com.example.stockgestion.exception.BusinessRuleException("La commande doit être au statut RESERVED pour libérer la réservation");
        }
        for (com.example.stockgestion.models.SalesOrderLine line : order.getLines()) {
            java.util.UUID productId = line.getProduct().getId();
            java.util.UUID warehouseId = line.getWarehouse().getId();
            long qty = line.getQuantity();
            com.example.stockgestion.models.Inventory inv = inventoryRepository.findByProduct_IdAndWarehouse_Id(productId, warehouseId)
                    .orElseThrow(() -> new com.example.stockgestion.exception.BusinessRuleException("Inventaire introuvable pour le produit et l'entrepôt"));
            if (inv.getQtyReserved() < qty) {
                throw new com.example.stockgestion.exception.BusinessRuleException("Impossible de libérer: réservation insuffisante");
            }
            inv.setQtyReserved(inv.getQtyReserved() - qty);
            inventoryRepository.save(inv);
        }
        order.setStatus(com.example.stockgestion.models.enums.SOStatus.CREATED);
        salesOrderRepository.save(order);
    }

    @Transactional
    public void transfer(com.example.stockgestion.Dto.request.TransferRequestDto dto) {
        if (dto.getFromWarehouseId().equals(dto.getToWarehouseId())) {
            throw new com.example.stockgestion.exception.BusinessRuleException("Transfert invalide: entrepôts identiques");
        }
        com.example.stockgestion.models.Product product = productRepository.findById(dto.getProductId()).orElseThrow(() ->
                new com.example.stockgestion.exception.ResourceNotFoundException("Product", "id", dto.getProductId())
        );
        com.example.stockgestion.models.WareHouse from = wareHouseRepository.findById(dto.getFromWarehouseId()).orElseThrow(() ->
                new com.example.stockgestion.exception.ResourceNotFoundException("WareHouse", "id", dto.getFromWarehouseId())
        );
        com.example.stockgestion.models.WareHouse to = wareHouseRepository.findById(dto.getToWarehouseId()).orElseThrow(() ->
                new com.example.stockgestion.exception.ResourceNotFoundException("WareHouse", "id", dto.getToWarehouseId())
        );
        com.example.stockgestion.models.Inventory invFrom = inventoryRepository.findByProduct_IdAndWarehouse_Id(dto.getProductId(), dto.getFromWarehouseId())
                .orElseThrow(() -> new com.example.stockgestion.exception.BusinessRuleException("Inventaire source introuvable"));
        com.example.stockgestion.models.Inventory invTo = inventoryRepository.findByProduct_IdAndWarehouse_Id(dto.getProductId(), dto.getToWarehouseId())
                .orElseGet(() -> {
                    com.example.stockgestion.models.Inventory i = new com.example.stockgestion.models.Inventory();
                    i.setProduct(product);
                    i.setWarehouse(to);
                    i.setQtyOnHand(0);
                    i.setQtyReserved(0);
                    return i;
                });
        long qty = dto.getQuantity();
        long dispoFrom = invFrom.getQtyOnHand() - invFrom.getQtyReserved();
        if (dispoFrom < qty) {
            throw new com.example.stockgestion.exception.BusinessRuleException("Transfert impossible: dispo source=" + dispoFrom + ", demandé=" + qty);
        }
        invFrom.setQtyOnHand(invFrom.getQtyOnHand() - qty);
        inventoryRepository.save(invFrom);

        invTo.setQtyOnHand(invTo.getQtyOnHand() + qty);
        inventoryRepository.save(invTo);

        // Log movements: OUTBOUND from, INBOUND to
        com.example.stockgestion.models.InventoryMovement mOut = new com.example.stockgestion.models.InventoryMovement();
        mOut.setProduct(product);
        mOut.setWarehouse(from);
        mOut.setType(com.example.stockgestion.models.enums.MovementType.OUTBOUND);
        mOut.setQuantity(-qty);
        mOut.setOccurredAt(java.time.Instant.now());
        mOut.setReferenceDoc(dto.getReferenceDoc());
        inventoryMovmentRepository.save(mOut);

        com.example.stockgestion.models.InventoryMovement mIn = new com.example.stockgestion.models.InventoryMovement();
        mIn.setProduct(product);
        mIn.setWarehouse(to);
        mIn.setType(com.example.stockgestion.models.enums.MovementType.INBOUND);
        mIn.setQuantity(qty);
        mIn.setOccurredAt(java.time.Instant.now());
        mIn.setReferenceDoc(dto.getReferenceDoc());
        inventoryMovmentRepository.save(mIn);
    }
}
