package com.example.stockgestion.Dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdjustmentRequestDto {
    @NotNull
    private UUID productId;
    @NotNull
    private UUID warehouseId;
    @NotNull
    private Long quantity; // can be positive or negative
    private String reason;
    private String referenceDoc;
    private Instant occurredAt;
}

