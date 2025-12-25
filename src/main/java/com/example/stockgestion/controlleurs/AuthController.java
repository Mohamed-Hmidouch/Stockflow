package com.example.stockgestion.controlleurs;

import com.example.stockgestion.Dto.request.LoginRequestDto;
import com.example.stockgestion.Dto.request.RefreshTokenRequestDto;
import com.example.stockgestion.Dto.response.AuthResponseDto;
import com.example.stockgestion.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Authentication controller for JWT-based authentication
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API d'authentification JWT")
public class AuthController {

    private final AuthService authService;

    /**
     * Login endpoint - Authenticate user and return JWT tokens
     */
    @PostMapping("/login")
    @Operation(summary = "Connexion", description = "Authentifier avec email/mot de passe et recevoir des tokens JWT")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody LoginRequestDto request) {
        try {
            AuthResponseDto response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new RuntimeException("Invalid credentials: " + e.getMessage());
        }
    }

    /**
     * Refresh token endpoint - Get new access token using refresh token
     */
    @PostMapping("/refresh")
    @Operation(summary = "Rafraîchir le token", description = "Obtenir un nouveau token d'accès avec le refresh token")
    public ResponseEntity<AuthResponseDto> refresh(@Valid @RequestBody RefreshTokenRequestDto request) {
        try {
            AuthResponseDto response = authService.refreshAccessToken(request.getRefreshToken());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            throw new RuntimeException("Invalid refresh token: " + e.getMessage());
        }
    }

    /**
     * Logout endpoint - Revoke refresh token
     */
    @PostMapping("/logout")
    @Operation(summary = "Déconnexion", description = "Révoquer le refresh token et se déconnecter")
    public ResponseEntity<Map<String, Object>> logout(@Valid @RequestBody RefreshTokenRequestDto request) {
        try {
            authService.logout(request.getRefreshToken());

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Déconnexion réussie");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Erreur lors de la déconnexion: " + e.getMessage());

            return ResponseEntity.ok(response);
        }
    }
}
