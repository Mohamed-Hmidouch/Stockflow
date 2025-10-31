package com.example.stockgestion.Dto.response;

import java.util.UUID;
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
public class SalesOrderSimpleResponseDto {
    private UUID id;
    private SOStatus status;
    private Instant createdAt;

    public SalesOrderSimpleResponseDto(SalesOrder order) {
        this.id = order.getId();
        this.status = order.getStatus();
        this.createdAt = order.getCreatedAt();
    }
}
