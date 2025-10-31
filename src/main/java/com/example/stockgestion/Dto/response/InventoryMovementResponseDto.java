package com.example.stockgestion.Dto.response;

import java.util.UUID;
import com.example.stockgestion.models.enums.MovementType;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.example.stockgestion.models.InventoryMovement;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryMovementResponseDto {
    private UUID id;
    private ProductResponseDto product;
    private WareHouseResponseDto warehouse;
    private MovementType type;
    private long quantity;
    private Instant occurredAt;
    private String referenceDoc;

    public InventoryMovementResponseDto(InventoryMovement movement) {
        this.id = movement.getId();
        this.product = new ProductResponseDto(movement.getProduct());
        this.warehouse = new WareHouseResponseDto(movement.getWarehouse());
        this.type = movement.getType();
        this.quantity = movement.getQuantity();
        this.occurredAt = movement.getOccurredAt();
        this.referenceDoc = movement.getReferenceDoc();
    }
}