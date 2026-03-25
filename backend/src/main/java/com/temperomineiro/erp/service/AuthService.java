package com.temperomineiro.erp.service;

import com.temperomineiro.erp.dto.AuthDto;
import com.temperomineiro.erp.exception.BusinessException;
import com.temperomineiro.erp.model.DomainEnums.RoleName;
import com.temperomineiro.erp.model.Restaurante;
import com.temperomineiro.erp.model.Role;
import com.temperomineiro.erp.model.User;
import com.temperomineiro.erp.repository.RestauranteRepository;
import com.temperomineiro.erp.repository.RoleRepository;
import com.temperomineiro.erp.repository.UserRepository;
import com.temperomineiro.erp.security.CustomUserDetails;
import com.temperomineiro.erp.security.JwtService;
import java.text.Normalizer;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Pattern NONLATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("[\\s]");

    private final RestauranteRepository restauranteRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final CredentialPolicyService credentialPolicyService;
    private final LoginAttemptService loginAttemptService;

    @Transactional
    public AuthDto.AuthResponse registerRestaurant(AuthDto.RegisterRestaurantRequest request) {
        credentialPolicyService.validatePasswordStrength(request.password());

        if (userRepository.existsByEmailIgnoreCase(request.adminEmail())) {
            throw new BusinessException("Já existe um usuário com este e-mail.");
        }

        String slug = toSlug(request.restaurantSlug());
        if (restauranteRepository.existsBySlugIgnoreCase(slug)) {
            throw new BusinessException("Slug do restaurante já está em uso.");
        }

        Restaurante restaurante = restauranteRepository.save(Restaurante.builder()
                .nome(request.restaurantName())
                .slug(slug)
                .ativo(true)
                .build());

        User user = User.builder()
                .restaurante(restaurante)
                .nome(request.adminName())
                .email(request.adminEmail().trim().toLowerCase(Locale.ROOT))
                .password(passwordEncoder.encode(request.password()))
                .ativo(true)
                .roles(Set.of(getRole(RoleName.ADMIN), getRole(RoleName.GERENTE)))
                .build();

        User savedUser = userRepository.save(user);
        return buildAuthResponse(savedUser);
    }

    @Transactional(readOnly = true)
    public AuthDto.AuthResponse login(AuthDto.LoginRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase(Locale.ROOT);
        loginAttemptService.assertLoginAllowed(normalizedEmail);

        User user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> invalidCredentials(normalizedEmail));

        if (!user.isAtivo() || !passwordEncoder.matches(request.password(), user.getPassword())) {
            throw invalidCredentials(normalizedEmail);
        }

        loginAttemptService.registerSuccess(normalizedEmail);
        return buildAuthResponse(user);
    }

    private AuthDto.AuthResponse buildAuthResponse(User user) {
        CustomUserDetails userDetails = new CustomUserDetails(user);
        return new AuthDto.AuthResponse(
                jwtService.generateToken(userDetails),
                new AuthDto.UserSummary(
                        user.getId(),
                        user.getNome(),
                        user.getEmail(),
                        user.getRestaurante().getNome(),
                        user.getRestaurante().getId(),
                        user.getRoles().stream().map(Role::getName).collect(java.util.stream.Collectors.toSet())
                )
        );
    }

    private Role getRole(RoleName roleName) {
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new BusinessException("Perfil " + roleName + " não configurado."));
    }

    private String toSlug(String input) {
        String trimmed = input == null ? "" : input.trim().toLowerCase(Locale.ROOT);
        String normalized = Normalizer.normalize(trimmed, Normalizer.Form.NFD);
        String withoutAccents = normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        String whitespaceReplaced = WHITESPACE.matcher(withoutAccents).replaceAll("-");
        return NONLATIN.matcher(whitespaceReplaced).replaceAll("").replaceAll("-{2,}", "-");
    }

    private BusinessException invalidCredentials(String normalizedEmail) {
        loginAttemptService.registerFailure(normalizedEmail);
        return new BusinessException("Credenciais inválidas.");
    }
}
