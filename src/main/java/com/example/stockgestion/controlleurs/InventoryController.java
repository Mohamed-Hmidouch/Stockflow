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


    @PostMapping("/outbound")
    public ResponseEntity<Void> outbound(@Valid @RequestBody com.example.stockgestion.Dto.request.OutboundRequestDto request) {
        inventoryService.outbound(request);
        return ResponseEntity.status(201).build();
    }

    @PostMapping("/adjustments")
    public ResponseEntity<com.example.stockgestion.Dto.response.InventoryMovementResponseDto> adjust(@Valid @RequestBody com.example.stockgestion.Dto.request.AdjustmentRequestDto request) {
        var dto = inventoryService.adjust(request);
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

    /**
     * GET /inventory/movements - list movements with optional filters
     */
    @GetMapping("/movements")
    public ResponseEntity<org.springframework.data.domain.Page<com.example.stockgestion.Dto.response.InventoryMovementResponseDto>> listMovements(
            @RequestParam(required = false) UUID productId,
            @RequestParam(required = false) UUID warehouseId,
            @RequestParam(required = false) com.example.stockgestion.models.enums.MovementType type,
            @RequestParam(required = false) java.time.Instant from,
            @RequestParam(required = false) java.time.Instant to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        var result = inventoryService.listMovements(productId, warehouseId, type, from, to, pageable);
        return ResponseEntity.ok(result);
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

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        inventoryService.deleteInventory(id);
        return ResponseEntity.noContent().build();
    }
}
