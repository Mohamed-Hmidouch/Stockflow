package com.example.stockgestion.Dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReceiveRequestDto {

    @NotNull(message = "L'ID de l'entrepôt ne peut pas être nul")
    private UUID warehouseId;

    @Valid
    @NotNull(message = "La liste des lignes ne peut pas être nulle")
    private List<ReceivedLineDto> receivedLineDto;
}
