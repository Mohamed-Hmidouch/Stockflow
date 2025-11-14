package com.example.stockgestion.controlleurs;

import com.example.stockgestion.Dto.request.ProductRequestDto;
import com.example.stockgestion.Dto.response.ProductResponseDto;
import com.example.stockgestion.exception.ResourceNotFoundException;
import com.example.stockgestion.models.Product;
import com.example.stockgestion.services.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ProductService productService;

    @Test
    void createProduct_ShouldReturnCreated() throws Exception {
        // Given
        ProductRequestDto requestDto = new ProductRequestDto();
        requestDto.setSku("TEST-SKU-001");
        requestDto.setName("Test Product");
        requestDto.setCategory("Electronics");
        requestDto.setPrice(BigDecimal.valueOf(100.00));

        ProductResponseDto responseDto = new ProductResponseDto();
        responseDto.setId(UUID.randomUUID());
        responseDto.setSku("TEST-SKU-001");
        responseDto.setName("Test Product");
        responseDto.setPrice(BigDecimal.valueOf(100.00));

        when(productService.createProduct(any(ProductRequestDto.class))).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(post("/api/products/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.sku").value("TEST-SKU-001"))
                .andExpect(jsonPath("$.name").value("Test Product"));

        verify(productService).createProduct(any(ProductRequestDto.class));
    }

    @Test
    void getProduct_ShouldReturnProduct() throws Exception {
        // Given
        UUID productId = UUID.randomUUID();
        ProductResponseDto responseDto = new ProductResponseDto();
        responseDto.setId(productId);
        responseDto.setSku("TEST-SKU-001");
        responseDto.setName("Test Product");

        when(productService.getProductById(productId)).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(get("/api/products/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(productId.toString()))
                .andExpect(jsonPath("$.sku").value("TEST-SKU-001"));

        verify(productService).getProductById(productId);
    }

    @Test
    void getProduct_WhenNotFound_ShouldReturn404() throws Exception {
        // Given
        UUID productId = UUID.randomUUID();
        when(productService.getProductById(productId))
                .thenThrow(new ResourceNotFoundException("Product not found"));

        // When & Then
        mockMvc.perform(get("/api/products/{id}", productId))
                .andExpect(status().isNotFound());

        verify(productService).getProductById(productId);
    }

    @Test
    void getAllProducts_ShouldReturnList() throws Exception {
        // Given
        ProductResponseDto product1 = new ProductResponseDto();
        product1.setId(UUID.randomUUID());
        product1.setSku("SKU-001");
        product1.setName("Product 1");

        ProductResponseDto product2 = new ProductResponseDto();
        product2.setId(UUID.randomUUID());
        product2.setSku("SKU-002");
        product2.setName("Product 2");

        List<ProductResponseDto> products = Arrays.asList(product1, product2);
        when(productService.getAllProducts()).thenReturn(products);

        // When & Then
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].sku").value("SKU-001"))
                .andExpect(jsonPath("$[1].sku").value("SKU-002"));

        verify(productService).getAllProducts();
    }

    @Test
    void updateProduct_ShouldReturnUpdated() throws Exception {
        // Given
        UUID productId = UUID.randomUUID();
        Product product = new Product();
        product.setSku("UPDATED-SKU");
        product.setName("Updated Product");
        product.setPrice(BigDecimal.valueOf(150.00));

        ProductResponseDto responseDto = new ProductResponseDto();
        responseDto.setId(productId);
        responseDto.setSku("UPDATED-SKU");
        responseDto.setName("Updated Product");
        responseDto.setPrice(BigDecimal.valueOf(150.00));

        when(productService.updateProduct(eq(productId), any(Product.class)))
                .thenReturn(responseDto);

        // When & Then
        mockMvc.perform(patch("/api/products/update/{id}", productId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(product)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("UPDATED-SKU"))
                .andExpect(jsonPath("$.name").value("Updated Product"));

        verify(productService).updateProduct(eq(productId), any(Product.class));
    }

    @Test
    void deleteProduct_ShouldReturnNoContent() throws Exception {
        // Given
        UUID productId = UUID.randomUUID();
        doNothing().when(productService).deleteProduct(productId);

        // When & Then
        mockMvc.perform(delete("/api/products/delete/{id}", productId))
                .andExpect(status().isNoContent());

        verify(productService).deleteProduct(productId);
    }

    @Test
    void deactivateProduct_ShouldReturnNoContent() throws Exception {
        // Given
        String sku = "TEST-SKU-001";
        ProductResponseDto responseDto = new ProductResponseDto();
        responseDto.setId(UUID.randomUUID());
        responseDto.setSku(sku);
        responseDto.setName("Test Product");
        responseDto.setActive(false);

        when(productService.deactivateProduct(sku)).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(patch("/api/products/{sku}/deactivate", sku))
                .andExpect(status().isNoContent());

        verify(productService).deactivateProduct(sku);
    }
}
