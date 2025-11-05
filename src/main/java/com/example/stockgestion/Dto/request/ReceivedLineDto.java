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
public class ReceivedLineDto {

    @NotNull(message = "L'ID du produit ne peut pas être nul")
    private UUID productId;

    @NotNull(message = "La ligne de commande d'achat ne peut pas être nulle")
    private UUID poLine;

    @NotNull(message = "La quantité reçue ne peut pas être nulle")
    @Positive(message = "La quantité reçue doit être positive")
    private Integer quantityReceived;
}
