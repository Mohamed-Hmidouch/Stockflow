package com.example.stockgestion.Dto.request;

import java.util.UUID;
import com.example.stockgestion.models.enums.ShipmentStatus;
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
public class ShipmentRequestDto {
    @NotNull
    private UUID salesOrderId;
    @NotNull
    private UUID carrierId;
    @NotNull
    private ShipmentStatus status;
    private String trackingNumber;
    private Instant shippedAt;
    private Instant deliveredAt;
}
