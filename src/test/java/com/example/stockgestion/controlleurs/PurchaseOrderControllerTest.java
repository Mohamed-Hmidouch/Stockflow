package com.example.stockgestion.controlleurs;

import com.example.stockgestion.Dto.request.PurchaseOrderLineRequestDto;
import com.example.stockgestion.Dto.request.PurchaseOrderRequestDto;
import com.example.stockgestion.Dto.request.ReceiveRequestDto;
import com.example.stockgestion.Dto.response.PurchaseOrderResponseDto;
import com.example.stockgestion.exception.ResourceNotFoundException;
import com.example.stockgestion.services.PurchaseOrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PurchaseOrderController.class)
class PurchaseOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PurchaseOrderService purchaseOrderService;

    @Test
    void createPurchaseOrder_ShouldReturnCreated() throws Exception {
        // Given
        UUID supplierId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        PurchaseOrderLineRequestDto lineDto = new PurchaseOrderLineRequestDto();
        lineDto.setProductId(productId);
        lineDto.setQuantity(100);
        lineDto.setUnitPrice(BigDecimal.valueOf(50.00));

        PurchaseOrderRequestDto requestDto = new PurchaseOrderRequestDto();
        requestDto.setSupplierId(supplierId);
        requestDto.setStatus(com.example.stockgestion.models.enums.POStatus.DRAFT);
        requestDto.setLines(Arrays.asList(lineDto));

        PurchaseOrderResponseDto responseDto = new PurchaseOrderResponseDto();
        responseDto.setId(UUID.randomUUID());

        when(purchaseOrderService.createPurchaseOrder(any(PurchaseOrderRequestDto.class)))
                .thenReturn(responseDto);

        // When & Then
        mockMvc.perform(post("/api/purchase-orders/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void getAllPurchaseOrders_ShouldReturnList() throws Exception {
        // Given
        PurchaseOrderResponseDto order1 = new PurchaseOrderResponseDto();
        order1.setId(UUID.randomUUID());

        PurchaseOrderResponseDto order2 = new PurchaseOrderResponseDto();
        order2.setId(UUID.randomUUID());

        List<PurchaseOrderResponseDto> orders = Arrays.asList(order1, order2);

        when(purchaseOrderService.getAllPurchaseOrders()).thenReturn(orders);

        // When & Then
        mockMvc.perform(get("/api/purchase-orders")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getPurchaseOrderById_ShouldReturnOrder() throws Exception {
        // Given
        UUID orderId = UUID.randomUUID();
        PurchaseOrderResponseDto responseDto = new PurchaseOrderResponseDto();
        responseDto.setId(orderId);

        when(purchaseOrderService.getPurchaseOrderById(orderId)).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(get("/api/purchase-orders/{id}", orderId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()));
    }

    @Test
    void getPurchaseOrderById_WhenNotFound_ShouldReturn404() throws Exception {
        // Given
        UUID orderId = UUID.randomUUID();

        when(purchaseOrderService.getPurchaseOrderById(orderId))
                .thenThrow(new ResourceNotFoundException("Purchase Order not found"));

        // When & Then
        mockMvc.perform(get("/api/purchase-orders/{id}", orderId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void receivePurchaseOrder_ShouldReturnOk() throws Exception {
        // Given
        UUID orderId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID poLineId = UUID.randomUUID();
        
        com.example.stockgestion.Dto.request.ReceivedLineDto receivedLineDto = 
                new com.example.stockgestion.Dto.request.ReceivedLineDto();
        receivedLineDto.setProductId(productId);
        receivedLineDto.setPoLine(poLineId);
        receivedLineDto.setQuantityReceived(50);
        
        ReceiveRequestDto requestDto = new ReceiveRequestDto();
        requestDto.setWarehouseId(UUID.randomUUID());
        requestDto.setReceivedLineDto(Arrays.asList(receivedLineDto));

        PurchaseOrderResponseDto responseDto = new PurchaseOrderResponseDto();
        responseDto.setId(orderId);

        when(purchaseOrderService.receptionOrder(any(UUID.class), any(ReceiveRequestDto.class)))
                .thenReturn(responseDto);

        // When & Then
        mockMvc.perform(post("/api/purchase-orders/receive/{orderId}", orderId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId.toString()));
    }
}
