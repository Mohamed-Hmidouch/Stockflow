package com.example.stockgestion.services;

import com.example.stockgestion.Dto.request.InventoryRequestDto;
import com.example.stockgestion.Dto.response.InventoryResponseDto;
import com.example.stockgestion.exception.ConflictException;
import com.example.stockgestion.exception.ResourceNotFoundException;
import com.example.stockgestion.models.Inventory;
import com.example.stockgestion.models.Product;
import com.example.stockgestion.models.WareHouse;
import com.example.stockgestion.repositories.InventoryRepository;
import com.example.stockgestion.repositories.ProductRepository;
import com.example.stockgestion.repositories.WareHouseRepository;
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
}
