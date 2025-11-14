package com.example.stockgestion.controlleurs;

import com.example.stockgestion.Dto.request.WareHouseRequestDto;
import com.example.stockgestion.Dto.response.WareHouseResponseDto;
import com.example.stockgestion.services.WarehouseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WarehouseController.class)
class WarehouseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WarehouseService warehouseService;

    @Test
    void getAllWarehouses_ShouldReturnList() throws Exception {
        // Given
        WareHouseResponseDto warehouse1 = new WareHouseResponseDto();
        warehouse1.setId(UUID.randomUUID());
        warehouse1.setName("Warehouse 1");
        warehouse1.setCode("WH-001");

        WareHouseResponseDto warehouse2 = new WareHouseResponseDto();
        warehouse2.setId(UUID.randomUUID());
        warehouse2.setName("Warehouse 2");
        warehouse2.setCode("WH-002");

        List<WareHouseResponseDto> warehouses = Arrays.asList(warehouse1, warehouse2);
        when(warehouseService.getAllWareHouses()).thenReturn(warehouses);

        // When & Then
        mockMvc.perform(get("/api/warehouses"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].code").value("WH-001"))
                .andExpect(jsonPath("$[1].code").value("WH-002"));

        verify(warehouseService).getAllWareHouses();
    }

    @Test
    void createWarehouse_ShouldReturnCreated() throws Exception {
        // Given
        WareHouseRequestDto requestDto = new WareHouseRequestDto();
        requestDto.setCode("WH-NEW");
        requestDto.setName("New Warehouse");

        WareHouseResponseDto responseDto = new WareHouseResponseDto();
        responseDto.setId(UUID.randomUUID());
        responseDto.setCode("WH-NEW");
        responseDto.setName("New Warehouse");

        when(warehouseService.createWareHouse(any(WareHouseRequestDto.class))).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(post("/api/warehouses/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("WH-NEW"));

        verify(warehouseService).createWareHouse(any(WareHouseRequestDto.class));
    }
}
