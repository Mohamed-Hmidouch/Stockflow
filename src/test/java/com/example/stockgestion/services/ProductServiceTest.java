package com.example.stockgestion.services;

import com.example.stockgestion.Dto.request.ProductRequestDto;
import com.example.stockgestion.Dto.response.ProductResponseDto;
import com.example.stockgestion.exception.ConflictException;
import com.example.stockgestion.exception.ResourceNotFoundException;
import com.example.stockgestion.models.Product;
import com.example.stockgestion.models.SalesOrder;
import com.example.stockgestion.models.SalesOrderLine;
import com.example.stockgestion.models.enums.SOStatus;
import com.example.stockgestion.repositories.ProductRepository;
import com.example.stockgestion.repositories.SalesOrderLineRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SalesOrderLineRepository salesOrderLineRepository;

    @InjectMocks
    private ProductService productService;

    private UUID productId;
    private Product product;
    private ProductRequestDto productRequestDto;

    @BeforeEach
    void setUp() {
        productId = UUID.randomUUID();
        
        product = new Product();
        product.setId(productId);
        product.setName("Test Product");
        product.setSku("TEST-SKU-001");
        product.setCategory("Electronics");
        product.setActive(true);
        product.setPrice(BigDecimal.valueOf(99.99));

        productRequestDto = new ProductRequestDto();
        productRequestDto.setName("New Product");
        productRequestDto.setSku("NEW-SKU-001");
        productRequestDto.setCategory("Electronics");
        productRequestDto.setActive(true);
        productRequestDto.setPrice(BigDecimal.valueOf(149.99));
    }

    @Test
    void createProduct_ShouldSucceed_WhenValidData() {
        // Given
        when(productRepository.existsBySku(productRequestDto.getSku())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        ProductResponseDto result = productService.createProduct(productRequestDto);

        // Then
        assertNotNull(result);
        verify(productRepository).existsBySku(productRequestDto.getSku());
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void createProduct_ShouldFail_WhenSkuAlreadyExists() {
        // Given
        when(productRepository.existsBySku(productRequestDto.getSku())).thenReturn(true);

        // When & Then
        ConflictException exception = assertThrows(ConflictException.class,
                () -> productService.createProduct(productRequestDto));
        
        assertTrue(exception.getMessage().contains("existe déjà"));
        verify(productRepository).existsBySku(productRequestDto.getSku());
        verify(productRepository, never()).save(any());
    }

    @Test
    void getProductById_ShouldSucceed_WhenProductExists() {
        // Given
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // When
        ProductResponseDto result = productService.getProductById(productId);

        // Then
        assertNotNull(result);
        assertEquals(product.getSku(), result.getSku());
        verify(productRepository).findById(productId);
    }

    @Test
    void getProductById_ShouldFail_WhenProductNotFound() {
        // Given
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> productService.getProductById(productId));
        
        verify(productRepository).findById(productId);
    }

    @Test
    void getAllProducts_ShouldReturnList_WhenProductsExist() {
        // Given
        Product product2 = new Product();
        product2.setId(UUID.randomUUID());
        product2.setSku("TEST-SKU-002");
        
        when(productRepository.findAll()).thenReturn(Arrays.asList(product, product2));

        // When
        List<ProductResponseDto> result = productService.getAllProducts();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(productRepository).findAll();
    }

    @Test
    void getAllProducts_ShouldReturnEmptyList_WhenNoProducts() {
        // Given
        when(productRepository.findAll()).thenReturn(Arrays.asList());

        // When
        List<ProductResponseDto> result = productService.getAllProducts();

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productRepository).findAll();
    }

    @Test
    void updateProduct_ShouldSucceed_WhenValidData() {
        // Given
        Product updateData = new Product();
        updateData.setName("Updated Product");
        updateData.setSku("TEST-SKU-001"); // Same SKU
        updateData.setPrice(BigDecimal.valueOf(199.99));
        
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        ProductResponseDto result = productService.updateProduct(productId, updateData);

        // Then
        assertNotNull(result);
        verify(productRepository).findById(productId);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void updateProduct_ShouldFail_WhenNewSkuAlreadyExists() {
        // Given
        Product updateData = new Product();
        updateData.setSku("EXISTING-SKU");
        
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.existsBySku("EXISTING-SKU")).thenReturn(true);

        // When & Then
        assertThrows(ConflictException.class,
                () -> productService.updateProduct(productId, updateData));
        
        verify(productRepository).findById(productId);
        verify(productRepository, never()).save(any());
    }

    @Test
    void updateProduct_ShouldFail_WhenProductNotFound() {
        // Given
        Product updateData = new Product();
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> productService.updateProduct(productId, updateData));
        
        verify(productRepository).findById(productId);
        verify(productRepository, never()).save(any());
    }

    @Test
    void updateProduct_ShouldUpdateOnlyProvidedFields() {
        // Given
        Product updateData = new Product();
        updateData.setName("Updated Name Only");
        // Other fields are null
        
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        // When
        ProductResponseDto result = productService.updateProduct(productId, updateData);

        // Then
        assertNotNull(result);
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void deleteProduct_ShouldSucceed_WhenProductExists() {
        // Given
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        doNothing().when(productRepository).delete(product);

        // When
        productService.deleteProduct(productId);

        // Then
        verify(productRepository).findById(productId);
        verify(productRepository).delete(product);
    }

    @Test
    void deleteProduct_ShouldFail_WhenProductNotFound() {
        // Given
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> productService.deleteProduct(productId));
        
        verify(productRepository).findById(productId);
        verify(productRepository, never()).delete(any());
    }

    @Test
    void deactivateProduct_ShouldFail_WhenProductNotFound() {
        // Given
        String sku = "NON-EXISTENT-SKU";
        when(productRepository.findBySku(sku)).thenReturn(null);

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> productService.deactivateProduct(sku));
        
        verify(productRepository).findBySku(sku);
    }

    @Test
    void deactivateProduct_ShouldFail_WhenHasActiveOrders() {
        // Given
        String sku = "TEST-SKU-001";
        when(productRepository.findBySku(sku)).thenReturn(product);
        when(productRepository.countActiveOrdersBySku(sku)).thenReturn(5);

        // When & Then
        ConflictException exception = assertThrows(ConflictException.class,
                () -> productService.deactivateProduct(sku));
        
        assertTrue(exception.getMessage().contains("commandes en cours"));
        verify(productRepository).findBySku(sku);
        verify(productRepository).countActiveOrdersBySku(sku);
    }

    @Test
    void deactivateProduct_ShouldFail_WhenNoSalesOrderLines() {
        // Given
        String sku = "TEST-SKU-001";
        when(productRepository.findBySku(sku)).thenReturn(product);
        when(productRepository.countActiveOrdersBySku(sku)).thenReturn(0);
        when(salesOrderLineRepository.findByProductId(productId)).thenReturn(Arrays.asList());

        // When & Then
        assertThrows(ResourceNotFoundException.class,
                () -> productService.deactivateProduct(sku));
        
        verify(salesOrderLineRepository).findByProductId(productId);
    }

    @Test
    void deactivateProduct_ShouldFail_WhenHasReservedStock() {
        // Given
        String sku = "TEST-SKU-001";
        SalesOrderLine orderLine = new SalesOrderLine();
        orderLine.setQtyReserved(10);
        orderLine.setProduct(product);
        
        SalesOrder salesOrder = new SalesOrder();
        salesOrder.setStatus(SOStatus.RESERVED);
        orderLine.setSalesOrder(salesOrder);
        
        when(productRepository.findBySku(sku)).thenReturn(product);
        when(productRepository.countActiveOrdersBySku(sku)).thenReturn(0);
        when(salesOrderLineRepository.findByProductId(productId)).thenReturn(Arrays.asList(orderLine));

        // When & Then
        ConflictException exception = assertThrows(ConflictException.class,
                () -> productService.deactivateProduct(sku));
        
        assertTrue(exception.getMessage().contains("ne peut pas être désactivé"));
    }
}
