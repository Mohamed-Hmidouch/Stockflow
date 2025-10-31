package com.example.stockgestion.Dto.request;

import java.util.List;
import java.util.UUID;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid; // IMPORTANT
import com.example.stockgestion.models.enums.POStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderRequestDto {

    @NotNull(message = "L'ID du fournisseur est requis")
    private UUID supplierId;
    
    @NotNull(message = "Le statut est requis (ex: DRAFT ou APPROVED)")
    private POStatus status;
    
    // PAS de createdAt (le serveur le gère)

    @Valid // Dit à Spring de valider AUSSI les objets dans cette liste
    @NotNull(message = "La liste des lignes ne peut pas être nulle")
    private List<PurchaseOrderLineRequestDto> lines;
}
