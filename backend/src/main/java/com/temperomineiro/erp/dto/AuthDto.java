package com.temperomineiro.erp.dto;

import com.temperomineiro.erp.model.DomainEnums.RoleName;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

public final class AuthDto {

    private AuthDto() {
    }

    public record LoginRequest(
            @Email @NotBlank String email,
            @NotBlank String password
    ) {
    }

    public record RegisterRestaurantRequest(
            @NotBlank String restaurantName,
            @NotBlank String restaurantSlug,
            @NotBlank String adminName,
            @Email @NotBlank String adminEmail,
            @NotBlank String password
    ) {
    }

    public record UserSummary(
            Long id,
            String nome,
            String email,
            String restaurante,
            Long restauranteId,
            Set<RoleName> roles
    ) {
    }

    public record AuthResponse(
            String token,
            UserSummary user
    ) {
    }

    public record CreateUserRequest(
            @NotBlank String nome,
            @Email @NotBlank String email,
            @NotBlank String password,
            @NotEmpty Set<RoleName> roles
    ) {
    }

    public record UpdateUserRequest(
            @NotBlank String nome,
            @Email @NotBlank String email,
            String password,
            @NotEmpty Set<RoleName> roles,
            boolean ativo
    ) {
    }
}
