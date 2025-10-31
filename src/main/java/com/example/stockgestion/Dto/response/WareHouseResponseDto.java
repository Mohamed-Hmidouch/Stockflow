package com.example.stockgestion.Dto.response;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.example.stockgestion.models.WareHouse;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WareHouseResponseDto {
    private UUID id;
    private String code;
    private String name;

    public WareHouseResponseDto(WareHouse warehouse) {
        this.id = warehouse.getId();
        this.code = warehouse.getCode();
        this.name = warehouse.getName();
    }
}