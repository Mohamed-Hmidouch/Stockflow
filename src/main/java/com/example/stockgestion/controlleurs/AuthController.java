package com.example.stockgestion.controlleurs;

import com.example.stockgestion.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Contrôleur d'authentification simple avec login/logout
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API de connexion/déconnexion")
public class AuthController {

    private final AuthService authService;

    /**
     * Login - Retourne les infos de l'utilisateur connecté avec SWITCH CASE
     */
    @PostMapping("/login")
    @Operation(summary = "Connexion", description = "Se connecter et récupérer les infos utilisateur")
    public ResponseEntity<Map<String, Object>> login(Authentication authentication, HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        if (authService.isAuthenticated()) {
            response.put("success", true);
            response.put("message", "Connexion réussie");
            response.put("username", authService.getCurrentUsername());
            response.put("roleInfo", authService.getRoleInfo()); // SWITCH CASE ici
            response.put("sessionId", session.getId());
        } else {
            response.put("success", false);
            response.put("message", "Non authentifié");
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Logout - Déconnecte l'utilisateur
     */
    @PostMapping("/logout")
    @Operation(summary = "Déconnexion", description = "Se déconnecter et invalider la session")
    public ResponseEntity<Map<String, Object>> logout(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            
            SecurityContextHolder.clearContext();
            
            response.put("success", true);
            response.put("message", "Déconnexion réussie");
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erreur lors de la déconnexion");
        }
        
        return ResponseEntity.ok(response);
    }

    /**
     * Info - Récupère les infos de session avec SWITCH CASE
     */
    @GetMapping("/info")
    @Operation(summary = "Informations", description = "Récupère les informations de l'utilisateur connecté")
    public ResponseEntity<Map<String, Object>> info(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        if (authService.isAuthenticated()) {
            response.put("authenticated", true);
            response.put("username", authService.getCurrentUsername());
            response.put("roleInfo", authService.getRoleInfo()); // SWITCH CASE ici
            response.put("sessionId", session.getId());
        } else {
            response.put("authenticated", false);
            response.put("message", "Non connecté");
        }
        
        return ResponseEntity.ok(response);
    }
}
