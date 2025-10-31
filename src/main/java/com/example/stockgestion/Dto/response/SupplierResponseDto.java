package com.example.stockgestion.Dto.response;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.example.stockgestion.models.Supplier;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SupplierResponseDto {
    private UUID id;
    private String name;
    private String contact;

    public SupplierResponseDto(Supplier supplier) {
        this.id = supplier.getId();
        this.name = supplier.getName();
        this.contact = supplier.getContact();
    }
}
