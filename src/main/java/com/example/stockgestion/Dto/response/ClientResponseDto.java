package com.example.stockgestion.Dto.response;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.example.stockgestion.models.Client;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientResponseDto {
    private UUID id;
    private String name;

    public ClientResponseDto(Client client) {
        this.id = client.getId();
        this.name = client.getName();
    }
}
