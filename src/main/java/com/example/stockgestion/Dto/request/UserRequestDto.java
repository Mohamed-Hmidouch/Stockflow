package com.example.stockgestion.Dto.request;

import java.util.UUID;
import com.example.stockgestion.models.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserRequestDto {
    @Email
    @NotBlank
    private String email;
    @NotBlank
    @Size(min = 8)
    private String passwordHash;
    private UUID clientId;
    @NotNull
    private Role role;
    private boolean active;
}
