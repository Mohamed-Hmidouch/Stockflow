package com.example.stockgestion.Dto.response;

import java.util.UUID;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.example.stockgestion.models.PurchaseOrderLine;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderLineResponseDto {
    private UUID id;
    private ProductResponseDto product;
    private long quantity;
    private BigDecimal unitPrice;

    public PurchaseOrderLineResponseDto(PurchaseOrderLine line) {
        this.id = line.getId();
        this.product = new ProductResponseDto(line.getProduct());
        this.quantity = line.getQuantity();
        this.unitPrice = line.getUnitPrice();
    }
}
