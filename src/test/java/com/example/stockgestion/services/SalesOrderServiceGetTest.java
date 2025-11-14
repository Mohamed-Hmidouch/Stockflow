package com.example.stockgestion.services;

import com.example.stockgestion.Dto.response.SalesOrderResponseDto;
import com.example.stockgestion.exception.ResourceNotFoundException;
import com.example.stockgestion.models.Client;
import com.example.stockgestion.models.SalesOrder;
import com.example.stockgestion.repositories.SalesOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalesOrderServiceGetTest {

    @Mock
    private SalesOrderRepository salesOrderRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private SalesOrderService salesOrderService;

    private UUID orderId;
    private SalesOrder salesOrder;
    private Client client;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        
        client = new Client();
        client.setId(UUID.randomUUID());
        client.setName("Test Client");
        
        salesOrder = new SalesOrder();
        salesOrder.setId(orderId);
        salesOrder.setClient(client);
    }

    @Test
    void getSalesOrderById_ShouldReturnOrder() {
        // Given
        when(salesOrderRepository.findById(orderId)).thenReturn(Optional.of(salesOrder));
        
        SalesOrderResponseDto responseDto = new SalesOrderResponseDto();
        responseDto.setId(orderId);
        
        when(modelMapper.map(any(SalesOrder.class), eq(SalesOrderResponseDto.class)))
                .thenReturn(responseDto);

        // When
        SalesOrderResponseDto result = salesOrderService.getSalesOrderById(orderId);

        // Then
        assertNotNull(result);
        assertEquals(orderId, result.getId());
        verify(salesOrderRepository).findById(orderId);
    }

    @Test
    void getSalesOrderById_WhenNotFound_ShouldThrowException() {
        // Given
        when(salesOrderRepository.findById(orderId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> salesOrderService.getSalesOrderById(orderId));
    }

    @Test
    void getAllSalesOrders_ShouldReturnList() {
        // Given
        Client client1 = new Client();
        client1.setId(UUID.randomUUID());
        
        SalesOrder order1 = new SalesOrder();
        order1.setId(UUID.randomUUID());
        order1.setClient(client1);
        order1.setLines(Arrays.asList());
        
        SalesOrder order2 = new SalesOrder();
        order2.setId(UUID.randomUUID());
        order2.setClient(client);
        order2.setLines(Arrays.asList());

        when(salesOrderRepository.findAll()).thenReturn(Arrays.asList(order1, order2));

        // When
        List<SalesOrderResponseDto> result = salesOrderService.getAllSalesOrders();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(salesOrderRepository).findAll();
    }

    @Test
    void getAllSalesOrders_WhenEmpty_ShouldReturnEmptyList() {
        // Given
        when(salesOrderRepository.findAll()).thenReturn(Arrays.asList());

        // When
        List<SalesOrderResponseDto> result = salesOrderService.getAllSalesOrders();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
