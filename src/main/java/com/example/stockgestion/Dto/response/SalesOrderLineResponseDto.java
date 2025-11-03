package com.example.stockgestion.Dto.response;

import java.util.UUID;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.example.stockgestion.models.SalesOrderLine;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SalesOrderLineResponseDto {
    private UUID id;
    private ProductResponseDto product;
    private WareHouseResponseDto warehouse;
    private long quantity;
    private long qtyReserved;
    private long qtyBackordered;
    private BigDecimal unitPrice;

    public SalesOrderLineResponseDto(SalesOrderLine line) {
        this.id = line.getId();
        this.product = new ProductResponseDto(line.getProduct());
        this.warehouse = new WareHouseResponseDto(line.getWarehouse());
        this.quantity = line.getQuantity();
        this.qtyReserved = line.getQtyReserved();
        this.qtyBackordered = line.getQtyBackordered();
        this.unitPrice = line.getUnitPrice();
    }
}
