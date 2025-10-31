package com.example.stockgestion.Dto.response;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.example.stockgestion.models.Inventory;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponseDto {
    private UUID id;
    private long qtyOnHand;
    private long qtyReserved;
    private ProductResponseDto product;
    private WareHouseResponseDto warehouse;

    public InventoryResponseDto(Inventory inventory) {
        this.id = inventory.getId();
        this.qtyOnHand = inventory.getQtyOnHand();
        this.qtyReserved = inventory.getQtyReserved();
        this.product = new ProductResponseDto(inventory.getProduct());
        this.warehouse = new WareHouseResponseDto(inventory.getWarehouse());
    }
}
