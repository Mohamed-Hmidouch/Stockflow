package com.example.stockgestion.Dto.response;

import java.util.UUID;
import com.example.stockgestion.models.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.example.stockgestion.models.User;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserResponseDto {
    private UUID id;
    private String email;
    private ClientResponseDto client; // ✅ Remplace clientId
    private Role role;
    private boolean active;

    public UserResponseDto(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.client = user.getClient() != null ? new ClientResponseDto(user.getClient()) : null;
        this.role = user.getRole();
        this.active = user.isActive();
    }
}