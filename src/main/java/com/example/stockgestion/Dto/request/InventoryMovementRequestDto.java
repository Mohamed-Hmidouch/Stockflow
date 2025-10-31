package com.example.stockgestion.Dto.request;

import java.util.UUID;
import com.example.stockgestion.models.enums.MovementType;
import java.time.Instant;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryMovementRequestDto {
    @NotNull
    private UUID productId;
    @NotNull
    private UUID warehouseId;
    @NotNull
    private MovementType type;
    @NotNull
    private long quantity;
    @NotNull
    private Instant occurredAt;
    private String referenceDoc;
}
