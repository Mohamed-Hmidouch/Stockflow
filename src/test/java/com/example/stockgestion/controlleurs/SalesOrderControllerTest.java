package com.example.stockgestion.controlleurs;

import com.example.stockgestion.Dto.request.SalesOrderRequestDto;
import com.example.stockgestion.Dto.response.SalesOrderResponseDto;
import com.example.stockgestion.services.SalesOrderService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SalesOrderController.class)
class SalesOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private SalesOrderService salesOrderService;

    @Test
    void getAllOrders_ShouldReturnList() throws Exception {
        SalesOrderResponseDto order1 = new SalesOrderResponseDto();
        order1.setId(UUID.randomUUID());

        when(salesOrderService.getAllSalesOrders()).thenReturn(Arrays.asList(order1));

        mockMvc.perform(get("/api/sales-orders"))
                .andExpect(status().isOk());

        verify(salesOrderService).getAllSalesOrders();
    }

    @Test
    void createOrder_ShouldReturnCreated() throws Exception {
        SalesOrderRequestDto requestDto = new SalesOrderRequestDto();
        SalesOrderResponseDto responseDto = new SalesOrderResponseDto();
        responseDto.setId(UUID.randomUUID());

        when(salesOrderService.createSalesOrder(any(SalesOrderRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/sales-orders/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated());

        verify(salesOrderService).createSalesOrder(any(SalesOrderRequestDto.class));
    }

    @Test
    void getOrder_ShouldReturnOrder() throws Exception {
        UUID id = UUID.randomUUID();
        SalesOrderResponseDto responseDto = new SalesOrderResponseDto();
        responseDto.setId(id);

        when(salesOrderService.getSalesOrderById(id)).thenReturn(responseDto);

        mockMvc.perform(get("/api/sales-orders/{id}", id))
                .andExpect(status().isOk());

        verify(salesOrderService).getSalesOrderById(id);
    }

    @Test
    void shipOrder_ShouldReturnOk() throws Exception {
        UUID id = UUID.randomUUID();
        SalesOrderResponseDto responseDto = new SalesOrderResponseDto();

        when(salesOrderService.shipOrder(id)).thenReturn(responseDto);

        mockMvc.perform(post("/api/sales-orders/{id}/ship", id))
                .andExpect(status().isOk());

        verify(salesOrderService).shipOrder(id);
    }

    @Test
    void cancelOrder_ShouldReturnOk() throws Exception {
        UUID id = UUID.randomUUID();
        SalesOrderResponseDto responseDto = new SalesOrderResponseDto();

        when(salesOrderService.cancelOrder(id)).thenReturn(responseDto);

        mockMvc.perform(post("/api/sales-orders/{id}/cancel", id))
                .andExpect(status().isOk());

        verify(salesOrderService).cancelOrder(id);
    }
}
