package com.example.stockgestion.controlleurs;

import com.example.stockgestion.Dto.request.PurchaseOrderRequestDto;
import com.example.stockgestion.Dto.request.ReceiveRequestDto;
import com.example.stockgestion.Dto.response.PurchaseOrderResponseDto;
import com.example.stockgestion.services.PurchaseOrderService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/purchase-orders")
@AllArgsConstructor
@Validated
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @PostMapping("/create")
    public ResponseEntity<PurchaseOrderResponseDto> createPurchaseOrder(@Valid @RequestBody PurchaseOrderRequestDto request) {
        PurchaseOrderResponseDto response = purchaseOrderService.createPurchaseOrder(request);
        return ResponseEntity.status(201).body(response);
    }

    @PostMapping("/receive/{orderId}")
    public ResponseEntity<PurchaseOrderResponseDto> receptionOrder(@PathVariable UUID orderId, @Valid @RequestBody ReceiveRequestDto request) {
        PurchaseOrderResponseDto response = purchaseOrderService.receptionOrder(orderId, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<PurchaseOrderResponseDto>> getAllPurchaseOrders() {
        List<PurchaseOrderResponseDto> response = purchaseOrderService.getAllPurchaseOrders();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrderResponseDto> getPurchaseOrderById(@PathVariable UUID id) {
        PurchaseOrderResponseDto response = purchaseOrderService.getPurchaseOrderById(id);
        return ResponseEntity.ok(response);
    }
}