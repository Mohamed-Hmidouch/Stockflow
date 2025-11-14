package com.example.stockgestion.services;

import com.example.stockgestion.Dto.request.SupplierRequestDto;
import com.example.stockgestion.Dto.response.SupplierResponseDto;
import com.example.stockgestion.exception.ResourceNotFoundException;
import com.example.stockgestion.models.Supplier;
import com.example.stockgestion.repositories.SupplierRepository;
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
class SupplierServiceTest {

    @Mock
    private SupplierRepository supplierRepository;

    @InjectMocks
    private SupplierService supplierService;

    private Supplier supplier;
    private UUID supplierId;

    @BeforeEach
    void setUp() {
        supplierId = UUID.randomUUID();
        supplier = new Supplier();
        supplier.setId(supplierId);
        supplier.setName("Test Supplier");
        supplier.setContact("test@supplier.com");
    }

    @Test
    void getAllSuppliers_ShouldReturnList() {
        // Given
        when(supplierRepository.findAll()).thenReturn(Arrays.asList(supplier));

        // When
        List<SupplierResponseDto> result = supplierService.getAllSuppliers();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(supplierRepository).findAll();
    }

    @Test
    void createSupplier_ShouldReturnCreatedSupplier() {
        // Given
        SupplierRequestDto requestDto = new SupplierRequestDto();
        requestDto.setName("New Supplier");
        requestDto.setContact("new@supplier.com");

        when(supplierRepository.save(any(Supplier.class))).thenReturn(supplier);

        // When
        SupplierResponseDto result = supplierService.createSupplier(requestDto);

        // Then
        assertNotNull(result);
        verify(supplierRepository).save(any(Supplier.class));
    }

    @Test
    void getSupplierById_ShouldReturnSupplier() {
        // Given
        when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(supplier));

        // When
        SupplierResponseDto result = supplierService.getSupplierById(supplierId);

        // Then
        assertNotNull(result);
        verify(supplierRepository).findById(supplierId);
    }

    @Test
    void getSupplierById_WhenNotFound_ShouldThrowException() {
        // Given
        when(supplierRepository.findById(supplierId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, 
            () -> supplierService.getSupplierById(supplierId));
    }

    @Test
    void updateSupplier_ShouldReturnUpdatedSupplier() {
        // Given
        SupplierRequestDto requestDto = new SupplierRequestDto();
        requestDto.setName("Updated Name");
        requestDto.setContact("updated@supplier.com");

        when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(supplier));
        when(supplierRepository.save(any(Supplier.class))).thenReturn(supplier);

        // When
        SupplierResponseDto result = supplierService.updateSupplier(supplierId, requestDto);

        // Then
        assertNotNull(result);
        verify(supplierRepository).findById(supplierId);
        verify(supplierRepository).save(any(Supplier.class));
    }
}

