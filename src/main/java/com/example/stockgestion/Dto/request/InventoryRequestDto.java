package com.example.stockgestion.Dto.request;

import java.util.UUID;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryRequestDto {
    @NotNull
    @Min(0)
    private long qtyOnHand;
    @NotNull
    @Min(0)
    private long qtyReserved;
    @NotNull
    private UUID productId;
    @NotNull
    private UUID warehouseId;
}
