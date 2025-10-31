package com.example.stockgestion.models;

import com.example.stockgestion.models.enums.Role; // Importez votre enum
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "users") // "users" est un nom standard
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;

    @Email // Valide que c'est un format email
    @NotBlank
    @Column(nullable = false, unique = true)
    private String email; // C'est le login

    @NotBlank
    @Column(nullable = false)
    private String passwordHash; // On stocke le mot de passe haché

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id") // Crée une colonne 'client_id' dans la table 'users'
    private Client client;

    // ----- LE RÔLE DE L'UTILISATEUR -----
    @NotNull
    @Enumerated(EnumType.STRING) // Stocke "ADMIN", "CLIENT", etc.
    @Column(nullable = false, length = 30)
    private Role role;

    @Column(nullable = false)
    private boolean active = true;
}