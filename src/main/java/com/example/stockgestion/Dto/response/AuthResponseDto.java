package com.example.stockgestion.Dto.response;

import com.example.stockgestion.models.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO pour la réponse d'authentification
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponseDto {
    
    private String username;
    private Role role;
    private List<String> permissions;
    private String message;
    private boolean authenticated;
}
