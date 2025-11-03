package com.example.stockgestion.services.helpers;

import com.example.stockgestion.exception.ResourceNotFoundException;
import com.example.stockgestion.models.Client;
import com.example.stockgestion.repositories.ClientRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Helper pour valider les clients
 */
@Component
@AllArgsConstructor
public class ClientValidator {
    private final ClientRepository clientRepository;

    /**
     * Valide et récupère un client par son ID
     */
    public Client validateAndGet(UUID clientId) {
        return clientRepository.findById(clientId)
            .orElseThrow(() -> new ResourceNotFoundException("Client", "id", clientId));
    }
}
