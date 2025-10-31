package com.example.stockgestion.Dto.response;

import java.util.UUID;
import java.util.List;
import com.example.stockgestion.models.enums.SOStatus;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.example.stockgestion.models.SalesOrder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SalesOrderResponseDto {
    private UUID id;
    private ClientResponseDto client; // ✅ Remplace clientId
    private SOStatus status;
    private Instant createdAt;
    private List<SalesOrderLineResponseDto> lines; // ✅ Ajouté
    private List<ShipmentResponseDto> shipments; // ✅ Ajouté (optionnel selon vos besoins)

    public SalesOrderResponseDto(SalesOrder order) {
        this.id = order.getId();
        this.client = new ClientResponseDto(order.getClient());
        this.status = order.getStatus();
        this.createdAt = order.getCreatedAt();
        this.lines = order.getLines().stream().map(SalesOrderLineResponseDto::new).toList();
        this.shipments = order.getShipments().stream().map(ShipmentResponseDto::new).toList();
    }
}