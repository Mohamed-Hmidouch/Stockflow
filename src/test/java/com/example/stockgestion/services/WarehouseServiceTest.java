package com.example.stockgestion.services;

import com.example.stockgestion.Dto.request.WareHouseRequestDto;
import com.example.stockgestion.Dto.response.WareHouseResponseDto;
import com.example.stockgestion.exception.ConflictException;
import com.example.stockgestion.exception.ResourceNotFoundException;
import com.example.stockgestion.models.WareHouse;
import com.example.stockgestion.repositories.WareHouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WarehouseServiceTest {

    @Mock
    private WareHouseRepository warehouseRepository;

    @InjectMocks
    private WarehouseService warehouseService;

    private UUID warehouseId;
    private WareHouse warehouse;
    private WareHouseRequestDto warehouseRequestDto;

    @BeforeEach
    void setUp() {
        warehouseId = UUID.randomUUID();
        
        warehouse = new WareHouse();
        warehouse.setId(warehouseId);
        warehouse.setName("Main Warehouse");
        warehouse.setCode("WH-MAIN-001");

        warehouseRequestDto = new WareHouseRequestDto();
        warehouseRequestDto.setName("New Warehouse");
        warehouseRequestDto.setCode("WH-NEW-001");
    }

    @Test
    void getWarehouseById_ShouldSucceed_WhenWarehouseExists() {
        // Given
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));

        // When
        WareHouseResponseDto result = warehouseService.getWarehouseById(warehouseId);

        // Then
        assertNotNull(result);
        assertEquals(warehouse.getCode(), result.getCode());
        assertEquals(warehouse.getName(), result.getName());
        verify(warehouseRepository).findById(warehouseId);
    }

    @Test
    void getWarehouseById_ShouldFail_WhenWarehouseNotFound() {
        // Given
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> warehouseService.getWarehouseById(warehouseId));
        
        assertTrue(exception.getMessage().contains("Warehouse"));
        verify(warehouseRepository).findById(warehouseId);
    }

    @Test
    void getAllWareHouses_ShouldReturnList_WhenWarehousesExist() {
        // Given
        WareHouse warehouse2 = new WareHouse();
        warehouse2.setId(UUID.randomUUID());
        warehouse2.setName("Secondary Warehouse");
        warehouse2.setCode("WH-SEC-001");
        
        when(warehouseRepository.findAll()).thenReturn(Arrays.asList(warehouse, warehouse2));

        // When
        List<WareHouseResponseDto> result = warehouseService.getAllWareHouses();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(warehouseRepository).findAll();
    }

    @Test
    void getAllWareHouses_ShouldFail_WhenNoWarehousesExist() {
        // Given
        when(warehouseRepository.findAll()).thenReturn(Arrays.asList());

        // When & Then
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
                () -> warehouseService.getAllWareHouses());
        
        assertTrue(exception.getMessage().contains("warehouses"));
        verify(warehouseRepository).findAll();
    }

    @Test
    void getAllWareHouses_ShouldFail_WhenListIsNull() {
        // Given
        when(warehouseRepository.findAll()).thenReturn(null);

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> warehouseService.getAllWareHouses());
        
        verify(warehouseRepository).findAll();
    }

    @Test
    void createWareHouse_ShouldSucceed_WhenValidData() {
        // Given
        when(warehouseRepository.existsByCode(warehouseRequestDto.getCode())).thenReturn(false);
        when(warehouseRepository.save(any(WareHouse.class))).thenReturn(warehouse);

        // When
        WareHouseResponseDto result = warehouseService.createWareHouse(warehouseRequestDto);

        // Then
        assertNotNull(result);
        verify(warehouseRepository).existsByCode(warehouseRequestDto.getCode());
        verify(warehouseRepository).save(any(WareHouse.class));
    }

    @Test
    void createWareHouse_ShouldFail_WhenCodeAlreadyExists() {
        // Given
        when(warehouseRepository.existsByCode(warehouseRequestDto.getCode())).thenReturn(true);

        // When & Then
        ConflictException exception = assertThrows(ConflictException.class,
                () -> warehouseService.createWareHouse(warehouseRequestDto));
        
        assertTrue(exception.getMessage().contains("deja excitée"));
        verify(warehouseRepository).existsByCode(warehouseRequestDto.getCode());
        verify(warehouseRepository, never()).save(any());
    }

    @Test
    void createWareHouse_ShouldSetCorrectFields() {
        // Given
        when(warehouseRepository.existsByCode(warehouseRequestDto.getCode())).thenReturn(false);
        when(warehouseRepository.save(any(WareHouse.class))).thenAnswer(invocation -> {
            WareHouse savedWarehouse = invocation.getArgument(0);
            savedWarehouse.setId(warehouseId);
            return savedWarehouse;
        });

        // When
        WareHouseResponseDto result = warehouseService.createWareHouse(warehouseRequestDto);

        // Then
        assertNotNull(result);
        verify(warehouseRepository).save(argThat(wh -> 
            wh.getCode().equals(warehouseRequestDto.getCode()) &&
            wh.getName().equals(warehouseRequestDto.getName())
        ));
    }
}
