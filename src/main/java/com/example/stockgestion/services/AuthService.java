package com.example.stockgestion.services;

import com.example.stockgestion.models.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

/**
 * Service d'authentification basique
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    /**
     * Récupère le nom d'utilisateur connecté
     */
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        
        return authentication.getName();
    }

    /**
     * Récupère le rôle de l'utilisateur connecté avec SWITCH CASE
     */
    public String getRoleInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return "Non connecté";
        }
        
        // Extraire le rôle
        String roleName = authentication.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("ROLE_CLIENT")
                .replace("ROLE_", "");
        
        Role role;
        try {
            role = Role.valueOf(roleName);
        } catch (IllegalArgumentException e) {
            role = Role.CLIENT;
        }
        
        // SWITCH CASE pour retourner l'info du rôle
        switch (role) {
            case ADMIN:
                return "Administrateur - Accès complet";
                
            case WAREHOUSE_MANAGER:
                return "Gestionnaire d'entrepôt - Gestion inventaire";
                
            case CLIENT:
                return "Client - Gestion des commandes";
                
            default:
                return "Rôle inconnu";
        }
    }

    /**
     * Vérifie si l'utilisateur est connecté
     */
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() 
                && !authentication.getName().equals("anonymousUser");
    }
}
