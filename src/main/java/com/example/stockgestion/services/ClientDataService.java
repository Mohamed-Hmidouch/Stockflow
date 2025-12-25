package com.example.stockgestion.services;

import com.example.stockgestion.models.User;
import com.example.stockgestion.models.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Service to enforce client data isolation
 * Ensures CLIENT role users can only access their own data
 */
@Service
@RequiredArgsConstructor
public class ClientDataService {

    private final UserDetailsServiceImpl userDetailsService;

    /**
     * Get the currently authenticated user
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("User not authenticated");
        }

        String email = authentication.getName();
        return userDetailsService.loadUserEntityByEmail(email);
    }

    /**
     * Ensure the current user can access data for the given client ID
     * If user is CLIENT role, throws exception if clientId doesn't match
     * If user is ADMIN or WAREHOUSE_MANAGER, always allows access
     */
    public void ensureClientAccess(UUID clientId) {
        User user = getCurrentUser();

        // ADMIN and WAREHOUSE_MANAGER can access all data
        if (user.getRole() == Role.ADMIN || user.getRole() == Role.WAREHOUSE_MANAGER) {
            return;
        }

        // CLIENT can only access their own data
        if (user.getRole() == Role.CLIENT) {
            if (user.getClient() == null) {
                throw new RuntimeException("CLIENT user has no associated client");
            }

            if (!user.getClient().getId().equals(clientId)) {
                throw new RuntimeException("Access denied: You can only access your own data");
            }
        }
    }

    /**
     * Get client filter for current user
     * Returns client ID if user is CLIENT role, empty otherwise
     * Use this to filter queries by client ID
     */
    public Optional<UUID> getClientFilter() {
        User user = getCurrentUser();

        if (user.getRole() == Role.CLIENT && user.getClient() != null) {
            return Optional.of(user.getClient().getId());
        }

        return Optional.empty();
    }

    /**
     * Get the current user's role
     */
    public Role getCurrentUserRole() {
        return getCurrentUser().getRole();
    }

    /**
     * Check if current user is CLIENT role
     */
    public boolean isClientRole() {
        return getCurrentUser().getRole() == Role.CLIENT;
    }
}
