package com.example.stockgestion.controlleurs;

import com.example.stockgestion.Dto.request.InventoryRequestDto;
import com.example.stockgestion.Dto.response.InventoryResponseDto;
import com.example.stockgestion.services.InventoryService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/inventory")
@AllArgsConstructor
@Validated
public class InventoryController {

    private final InventoryService inventoryService;

    /**
     * Endpoint requested: POST /inventory/movements/create
     * For now this creates an Inventory record using InventoryRequestDto.
     */
    @PostMapping("/movements/create")
    public ResponseEntity<com.example.stockgestion.Dto.response.InventoryMovementResponseDto> createMovement(@Valid @RequestBody com.example.stockgestion.Dto.request.InventoryMovementRequestDto request) {
        com.example.stockgestion.Dto.response.InventoryMovementResponseDto dto = inventoryService.createMovement(request);
        return ResponseEntity.status(201).body(dto);
    }

    /**
     * Create inventory resource (POST /api/inventory)
     * This endpoint creates an inventory record (not a movement) and returns 201.
     */
    @PostMapping
    public ResponseEntity<InventoryResponseDto> createInventory(@Valid @RequestBody InventoryRequestDto request) {
        InventoryResponseDto dto = inventoryService.createInventory(request);
        return ResponseEntity.status(201).body(dto);
    }

    @GetMapping
    public ResponseEntity<List<InventoryResponseDto>> getAll() {
        List<InventoryResponseDto> list = inventoryService.getAllInventories();
        return ResponseEntity.ok(list);
    }

    /**
     * GET /inventory/product/{productId} - list inventories for a product
     */
    @GetMapping("/product/{productId}")
    public ResponseEntity<List<InventoryResponseDto>> getByProduct(@PathVariable UUID productId) {
        List<InventoryResponseDto> list = inventoryService.getByProductId(productId);
        return ResponseEntity.ok(list);
    }

    /**
     * GET /inventory/warehouse/{warehouseId} - list inventories for a warehouse
     */
    @GetMapping("/warehouse/{warehouseId}")
    public ResponseEntity<List<InventoryResponseDto>> getByWarehouse(@PathVariable UUID warehouseId) {
        List<InventoryResponseDto> list = inventoryService.getByWarehouseId(warehouseId);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryResponseDto> getById(@PathVariable UUID id) {
        InventoryResponseDto dto = inventoryService.getInventoryById(id);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<InventoryResponseDto> update(@PathVariable UUID id, @Valid @RequestBody InventoryRequestDto request) {
        InventoryResponseDto dto = inventoryService.updateInventory(id, request);
        return ResponseEntity.ok(dto);
    }
}
