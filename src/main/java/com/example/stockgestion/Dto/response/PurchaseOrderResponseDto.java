package com.example.stockgestion.Dto.response;

import java.util.UUID;
import com.example.stockgestion.models.enums.POStatus;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;
import com.example.stockgestion.models.PurchaseOrder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderResponseDto {
    private UUID id;
    private SupplierResponseDto supplier;
    private POStatus status;
    private Instant createdAt;
    private List<PurchaseOrderLineResponseDto> lines;

    public PurchaseOrderResponseDto(PurchaseOrder order) {
        this.id = order.getId();
        this.supplier = new SupplierResponseDto(order.getSupplier());
        this.status = order.getStatus();
        this.createdAt = order.getCreatedAt();
        this.lines = order.getLines().stream().map(PurchaseOrderLineResponseDto::new).toList();
    }
}
