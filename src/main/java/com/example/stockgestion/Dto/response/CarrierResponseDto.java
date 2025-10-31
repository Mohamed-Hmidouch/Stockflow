package com.example.stockgestion.Dto.response;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.example.stockgestion.models.Carrier;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CarrierResponseDto {
    private UUID id;
    private String name;
    private boolean active;

    public CarrierResponseDto(Carrier carrier) {
        this.id = carrier.getId();
        this.name = carrier.getName();
        this.active = carrier.isActive();
    }
}
