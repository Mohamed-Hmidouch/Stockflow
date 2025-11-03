package com.example.stockgestion.Dto.request;

import java.util.UUID;
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
    
    // PAS de warehouseId - le service trouvera automatiquement le warehouse avec le plus de stock

    @Positive(message = "La quantité doit être positive")
    private long quantity;
    
    // PAS de unitPrice - SÉCURITÉ: le prix est récupéré depuis la database (Product.price)
    // On ne fait JAMAIS confiance au prix envoyé par le client!
}
