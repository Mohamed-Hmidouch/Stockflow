package com.example.stockgestion.controlleurs;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.example.stockgestion.Dto.request.WareHouseRequestDto;
import com.example.stockgestion.Dto.response.WareHouseResponseDto;
import com.example.stockgestion.services.WarehouseService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("api/warehouses")
@RequiredArgsConstructor
@Validated
public class WarehouseController {
    private final WarehouseService warehouseService;

    @GetMapping
    public ResponseEntity<List<WareHouseResponseDto>> getAllWarehouses(){
        List<WareHouseResponseDto> warehouses = warehouseService.getAllWareHouses();
        return ResponseEntity.ok(warehouses);
    }
    
    @PostMapping("create")
    public ResponseEntity<WareHouseResponseDto> createWarehouse(@RequestBody WareHouseRequestDto requestDto) {
        WareHouseResponseDto createdWarehouse = warehouseService.createWareHouse(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdWarehouse);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WareHouseResponseDto> getWarehouseById(@PathVariable UUID id) {
        WareHouseResponseDto warehouse = warehouseService.getWarehouseById(id);
        return ResponseEntity.ok(warehouse);
    }
}