package com.example.stockgestion.Dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductRequestDto {
    @NotBlank
    @Size(min = 3, max = 20)
    private String sku;
    @NotBlank
    @Size(min = 2, max = 100)
    private String name;
    private String category;
    private boolean active;
    private BigDecimal price;
}
