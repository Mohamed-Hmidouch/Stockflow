package com.example.stockgestion.Dto.request;

import java.util.UUID;
import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SalesOrderLineRequestDto {
    
    // PAS de salesOrderId (implicite)
    
    @NotNull(message = "L'ID du produit est requis")
    private UUID productId;
    
    // PAS de warehouseId (le service le trouvera)

    @Positive(message = "La quantité doit être positive")
    private long quantity;
    
    @NotNull(message = "Le prix unitaire est requis")
    @DecimalMin(value = "0.0", inclusive = false, message = "Le prix unitaire doit être positif")
    private BigDecimal unitPrice;
}
