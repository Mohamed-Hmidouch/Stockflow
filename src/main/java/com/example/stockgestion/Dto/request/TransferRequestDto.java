package com.example.stockgestion.Dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransferRequestDto {
    @NotNull
    private UUID productId;
    @NotNull
    private UUID fromWarehouseId;
    @NotNull
    private UUID toWarehouseId;
    @Positive
    private long quantity;
    private String referenceDoc;
}
