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
    @NotNull(message = "L'ID de la commande est obligatoire")
    private UUID salesOrderId;
    
    @NotNull(message = "L'ID du transporteur est obligatoire")
    private UUID carrierId;
    
    private String trackingNumber;
    
    private Integer cutoffHour; // Heure limite (par défaut 14h si non fourni)
}
