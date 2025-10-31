package com.example.stockgestion.Dto.response;

import java.util.UUID;
import com.example.stockgestion.models.enums.ShipmentStatus;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.example.stockgestion.models.Shipment;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentResponseDto {
    private UUID id;
    private SalesOrderSimpleResponseDto salesOrder;
    private CarrierResponseDto carrier;
    private ShipmentStatus status;
    private String trackingNumber;
    private Instant shippedAt;
    private Instant deliveredAt;

    public ShipmentResponseDto(Shipment shipment) {
        this.id = shipment.getId();
        this.salesOrder = new SalesOrderSimpleResponseDto(shipment.getSalesOrder());
        this.carrier = new CarrierResponseDto(shipment.getCarrier());
        this.status = shipment.getStatus();
        this.trackingNumber = shipment.getTrackingNumber();
        this.shippedAt = shipment.getShippedAt();
        this.deliveredAt = shipment.getDeliveredAt();
    }
}
