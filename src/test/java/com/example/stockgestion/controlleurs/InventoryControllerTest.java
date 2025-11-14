package com.example.stockgestion.controlleurs;

import com.example.stockgestion.Dto.request.InventoryRequestDto;
import com.example.stockgestion.Dto.response.InventoryResponseDto;
import com.example.stockgestion.services.InventoryService;
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

@WebMvcTest(InventoryController.class)
class InventoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InventoryService inventoryService;

    @Test
    void getAllInventories_ShouldReturnList() throws Exception {
        InventoryResponseDto inv1 = new InventoryResponseDto();
        inv1.setId(UUID.randomUUID());

        when(inventoryService.getAllInventories()).thenReturn(Arrays.asList(inv1));

        mockMvc.perform(get("/api/inventories"))
                .andExpect(status().isOk());

        verify(inventoryService).getAllInventories();
    }

    @Test
    void createInventory_ShouldReturnCreated() throws Exception {
        InventoryRequestDto requestDto = new InventoryRequestDto();
        InventoryResponseDto responseDto = new InventoryResponseDto();
        responseDto.setId(UUID.randomUUID());

        when(inventoryService.createInventory(any(InventoryRequestDto.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/inventories/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated());

        verify(inventoryService).createInventory(any(InventoryRequestDto.class));
    }

    @Test
    void getInventory_ShouldReturnInventory() throws Exception {
        UUID id = UUID.randomUUID();
        InventoryResponseDto responseDto = new InventoryResponseDto();
        responseDto.setId(id);

        when(inventoryService.getInventoryById(id)).thenReturn(responseDto);

        mockMvc.perform(get("/api/inventories/{id}", id))
                .andExpect(status().isOk());

        verify(inventoryService).getInventoryById(id);
    }
}
