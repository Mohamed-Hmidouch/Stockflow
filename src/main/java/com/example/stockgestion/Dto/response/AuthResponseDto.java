package com.example.stockgestion.Dto.response;

import com.example.stockgestion.models.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO pour la réponse d'authentification
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponseDto {

    private String email;
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;
    private Role role;
    private String clientId;
}
