package com.example.stockgestion.services;

import com.example.stockgestion.Dto.request.PurchaseOrderLineRequestDto;
import com.example.stockgestion.Dto.request.PurchaseOrderRequestDto;
import com.example.stockgestion.Dto.response.PurchaseOrderResponseDto;
import com.example.stockgestion.exception.ResourceNotFoundException;
import com.example.stockgestion.models.*;
import com.example.stockgestion.models.enums.POStatus;
import com.example.stockgestion.repositories.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseOrderServiceTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;
    @Mock
    private PurchaseOrderLineRepository purchaseOrderLineRepository;
    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private SupplierRepository supplierRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private WareHouseRepository wareHouseRepository;
    @Mock
    private InventoryMovmentRepository inventoryMovmentRepository;

    @InjectMocks
    private PurchaseOrderService purchaseOrderService;

    private UUID supplierId, productId, orderId;
    private Supplier supplier;
    private Product product;
    private PurchaseOrder purchaseOrder;

    @BeforeEach
    void setUp() {
        supplierId = UUID.randomUUID();
        productId = UUID.randomUUID();
        orderId = UUID.randomUUID();

        supplier = new Supplier();
        supplier.setId(supplierId);
        supplier.setName("Test Supplier");

        product = new Product();
        product.setId(productId);
        product.setSku("TEST-SKU");
        product.setName("Test Product");

        purchaseOrder = new PurchaseOrder();
        purchaseOrder.setId(orderId);
        purchaseOrder.setSupplier(supplier);
        purchaseOrder.setStatus(POStatus.APPROVED);
    }

    @Test
    void createPurchaseOrder_ShouldReturnCreatedOrder() {
        // Given
        PurchaseOrderLineRequestDto lineDto = new PurchaseOrderLineRequestDto();
        lineDto.setProductId(productId);
        lineDto.setQuantity(100);
        lineDto.setUnitPrice(BigDecimal.valueOf(50.00));

        PurchaseOrderRequestDto requestDto = new PurchaseOrderRequestDto();
        requestDto.setSupplierId(supplierId);
        requestDto.setLines(Arrays.asList(lineDto));

        PurchaseOrderResponseDto responseDto = new PurchaseOrderResponseDto();

        when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(supplier));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(purchaseOrder);
        when(modelMapper.map(any(PurchaseOrder.class), eq(PurchaseOrderResponseDto.class)))
                .thenReturn(responseDto);

        // When
        PurchaseOrderResponseDto result = purchaseOrderService.createPurchaseOrder(requestDto);

        // Then
        assertNotNull(result);
        verify(supplierRepository).findById(supplierId);
        verify(productRepository).findById(productId);
        verify(purchaseOrderRepository).save(any(PurchaseOrder.class));
    }

    @Test
    void createPurchaseOrder_WhenSupplierNotFound_ShouldThrowException() {
        // Given
        PurchaseOrderRequestDto requestDto = new PurchaseOrderRequestDto();
        requestDto.setSupplierId(supplierId);
        requestDto.setLines(Arrays.asList());

        when(supplierRepository.findById(supplierId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> purchaseOrderService.createPurchaseOrder(requestDto));
    }

    @Test
    void getAllPurchaseOrders_ShouldReturnList() {
        // Given
        when(purchaseOrderRepository.findAll()).thenReturn(Arrays.asList(purchaseOrder));

        PurchaseOrderResponseDto responseDto = new PurchaseOrderResponseDto();
        when(modelMapper.map(any(PurchaseOrder.class), eq(PurchaseOrderResponseDto.class)))
                .thenReturn(responseDto);

        // When
        List<PurchaseOrderResponseDto> result = purchaseOrderService.getAllPurchaseOrders();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(purchaseOrderRepository).findAll();
    }

    @Test
    void getPurchaseOrderById_ShouldReturnOrder() {
        // Given
        when(purchaseOrderRepository.findById(orderId)).thenReturn(Optional.of(purchaseOrder));
        
        PurchaseOrderResponseDto responseDto = new PurchaseOrderResponseDto();
        when(modelMapper.map(any(PurchaseOrder.class), eq(PurchaseOrderResponseDto.class)))
                .thenReturn(responseDto);

        // When
        PurchaseOrderResponseDto result = purchaseOrderService.getPurchaseOrderById(orderId);

        // Then
        assertNotNull(result);
        verify(purchaseOrderRepository).findById(orderId);
    }

    @Test
    void getPurchaseOrderById_WhenNotFound_ShouldThrowException() {
        // Given
        when(purchaseOrderRepository.findById(orderId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> purchaseOrderService.getPurchaseOrderById(orderId));
    }

    @Test
    void createPurchaseOrder_WhenProductNotFound_ShouldThrowException() {
        // Given
        PurchaseOrderLineRequestDto lineDto = new PurchaseOrderLineRequestDto();
        lineDto.setProductId(productId);
        lineDto.setQuantity(100);
        lineDto.setUnitPrice(BigDecimal.valueOf(50.00));

        PurchaseOrderRequestDto requestDto = new PurchaseOrderRequestDto();
        requestDto.setSupplierId(supplierId);
        requestDto.setLines(Arrays.asList(lineDto));

        when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(supplier));
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> purchaseOrderService.createPurchaseOrder(requestDto));
    }

    @Test
    void createPurchaseOrder_WithMultipleLines_ShouldCreateAllLines() {
        // Given
        UUID productId2 = UUID.randomUUID();
        Product product2 = new Product();
        product2.setId(productId2);
        product2.setSku("TEST-SKU-2");

        PurchaseOrderLineRequestDto lineDto1 = new PurchaseOrderLineRequestDto();
        lineDto1.setProductId(productId);
        lineDto1.setQuantity(100);
        lineDto1.setUnitPrice(BigDecimal.valueOf(50.00));

        PurchaseOrderLineRequestDto lineDto2 = new PurchaseOrderLineRequestDto();
        lineDto2.setProductId(productId2);
        lineDto2.setQuantity(50);
        lineDto2.setUnitPrice(BigDecimal.valueOf(25.00));

        PurchaseOrderRequestDto requestDto = new PurchaseOrderRequestDto();
        requestDto.setSupplierId(supplierId);
        requestDto.setLines(Arrays.asList(lineDto1, lineDto2));

        PurchaseOrderResponseDto responseDto = new PurchaseOrderResponseDto();

        when(supplierRepository.findById(supplierId)).thenReturn(Optional.of(supplier));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.findById(productId2)).thenReturn(Optional.of(product2));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(purchaseOrder);
        when(modelMapper.map(any(PurchaseOrder.class), eq(PurchaseOrderResponseDto.class)))
                .thenReturn(responseDto);

        // When
        PurchaseOrderResponseDto result = purchaseOrderService.createPurchaseOrder(requestDto);

        // Then
        assertNotNull(result);
        verify(productRepository, times(2)).findById(any(UUID.class));
    }

    @Test
    void getAllPurchaseOrders_WithMultipleOrders_ShouldReturnAll() {
        // Given
        PurchaseOrder order2 = new PurchaseOrder();
        order2.setId(UUID.randomUUID());
        
        when(purchaseOrderRepository.findAll()).thenReturn(Arrays.asList(purchaseOrder, order2));

        PurchaseOrderResponseDto responseDto1 = new PurchaseOrderResponseDto();
        PurchaseOrderResponseDto responseDto2 = new PurchaseOrderResponseDto();
        
        when(modelMapper.map(eq(purchaseOrder), eq(PurchaseOrderResponseDto.class)))
                .thenReturn(responseDto1);
        when(modelMapper.map(eq(order2), eq(PurchaseOrderResponseDto.class)))
                .thenReturn(responseDto2);

        // When
        List<PurchaseOrderResponseDto> result = purchaseOrderService.getAllPurchaseOrders();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(purchaseOrderRepository).findAll();
    }

    @Test
    void getAllPurchaseOrders_WhenEmpty_ShouldReturnEmptyList() {
        // Given
        when(purchaseOrderRepository.findAll()).thenReturn(Arrays.asList());

        // When
        List<PurchaseOrderResponseDto> result = purchaseOrderService.getAllPurchaseOrders();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
