package com.example.stockgestion.services;

import com.example.stockgestion.Dto.response.WareHouseResponseDto;
import com.example.stockgestion.Dto.request.WareHouseRequestDto;
import com.example.stockgestion.models.WareHouse;
import com.example.stockgestion.repositories.WareHouseRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.stockgestion.exception.ConflictException;
import com.example.stockgestion.exception.ResourceNotFoundException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;


@RequiredArgsConstructor
@Service
public class WarehouseService {
    private final WareHouseRepository warehouseRepository;

    @Transactional(readOnly = true)
    public WareHouseResponseDto getWarehouseById(UUID id){
        WareHouse wareHouse = warehouseRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Warehouse", "id", id));
        return new WareHouseResponseDto(wareHouse);
    }

    @Transactional(readOnly = true)
    public List<WareHouseResponseDto> getAllWareHouses(){
        List<WareHouse> wareHouses = warehouseRepository.findAll();
        if (wareHouses == null || wareHouses.isEmpty()) {
            throw new ResourceNotFoundException("list of warehouses");
        }
        return wareHouses.stream()
                .map(WareHouseResponseDto::new)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public WareHouseResponseDto createWareHouse(WareHouseRequestDto wareHouseDto){
        if(warehouseRepository.existsByCode(wareHouseDto.getCode())){
            throw new ConflictException("ce warehouse avec ce code est deja excitée");
        }
        WareHouse wareHouse = new WareHouse();
        wareHouse.setCode(wareHouseDto.getCode());
        wareHouse.setName(wareHouseDto.getName());
        WareHouse wareHouseSaved = warehouseRepository.save(wareHouse);
        return new WareHouseResponseDto(wareHouseSaved);
    }
}
