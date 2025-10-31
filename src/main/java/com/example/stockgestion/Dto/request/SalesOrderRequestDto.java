package com.example.stockgestion.Dto.request;

import java.util.List;
import java.util.UUID;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid; // IMPORTANT
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SalesOrderRequestDto {
    
    @NotNull(message = "L'ID du client est requis")
    private UUID clientId;

    // PAS de status ou createdAt (le serveur les gère)

    @Valid // Dit à Spring de valider AUSSI les objets dans cette liste
    @NotNull(message = "La liste des lignes ne peut pas être nulle")
    private List<SalesOrderLineRequestDto> lines;
}
