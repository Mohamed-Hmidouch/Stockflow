package com.example.stockgestion.Dto.response;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.example.stockgestion.models.Product;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDto {
    private UUID id;
    private String sku;
    private String name;
    private String category;
    private Boolean active;
    private BigDecimal price;

    public ProductResponseDto(Product product) {
        this.id = product.getId();
        this.sku = product.getSku();
        this.name = product.getName();
        this.category = product.getCategory();
        this.active = product.getActive();
        this.price = product.getPrice();
    }
}
