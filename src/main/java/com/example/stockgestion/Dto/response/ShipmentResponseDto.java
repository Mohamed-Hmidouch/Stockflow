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
    private Instant plannedDepartureDate;
    private Instant actualDepartureDate;
    private Instant shippedAt;
    private Instant deliveredAt;
    private Integer cutoffHour;

    public ShipmentResponseDto(Shipment shipment) {
        this.id = shipment.getId();
        this.salesOrder = new SalesOrderSimpleResponseDto(shipment.getSalesOrder());
        this.carrier = new CarrierResponseDto(shipment.getCarrier());
        this.status = shipment.getStatus();
        this.trackingNumber = shipment.getTrackingNumber();
        this.plannedDepartureDate = shipment.getPlannedDepartureDate();
        this.actualDepartureDate = shipment.getActualDepartureDate();
        this.shippedAt = shipment.getShippedAt();
        this.deliveredAt = shipment.getDeliveredAt();
        this.cutoffHour = shipment.getCutoffHour();
    }
}
