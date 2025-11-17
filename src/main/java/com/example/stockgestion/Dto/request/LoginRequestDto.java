package com.example.stockgestion.Dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour la requête de connexion
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDto {
    
    @NotBlank(message = "Le nom d'utilisateur est obligatoire")
    private String username;
    
    @NotBlank(message = "Le mot de passe est obligatoire")
    private String password;
}
