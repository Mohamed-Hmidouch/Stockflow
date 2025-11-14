package com.example.stockgestion.services.helpers;

import com.example.stockgestion.exception.ResourceNotFoundException;
import com.example.stockgestion.models.Client;
import com.example.stockgestion.repositories.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientValidatorTest {

    @Mock
    private ClientRepository clientRepository;

    @InjectMocks
    private ClientValidator clientValidator;

    private UUID clientId;
    private Client client;

    @BeforeEach
    void setUp() {
        clientId = UUID.randomUUID();
        client = new Client();
        client.setId(clientId);
        client.setName("Test Client");
    }

    @Test
    void validateAndGet_ShouldReturnClient_WhenClientExists() {
        // Given
        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));

        // When
        Client result = clientValidator.validateAndGet(clientId);

        // Then
        assertNotNull(result);
        assertEquals(clientId, result.getId());
        assertEquals("Test Client", result.getName());
        verify(clientRepository).findById(clientId);
    }

    @Test
    void validateAndGet_ShouldThrowException_WhenClientNotFound() {
        // Given
        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        // When & Then
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> clientValidator.validateAndGet(clientId)
        );

        assertTrue(exception.getMessage().contains("Client"));
        assertTrue(exception.getMessage().contains("id"));
        verify(clientRepository).findById(clientId);
    }
}
