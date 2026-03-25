package com.temperomineiro.erp.service;

import com.temperomineiro.erp.dto.AuthDto;
import com.temperomineiro.erp.dto.CommonDto;
import com.temperomineiro.erp.exception.BusinessException;
import com.temperomineiro.erp.exception.ResourceNotFoundException;
import com.temperomineiro.erp.model.DomainEnums.RoleName;
import com.temperomineiro.erp.model.Role;
import com.temperomineiro.erp.model.User;
import com.temperomineiro.erp.repository.RoleRepository;
import com.temperomineiro.erp.repository.UserRepository;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AuthContextService authContextService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final CredentialPolicyService credentialPolicyService;
    private final PageMapperService pageMapperService;

    @Transactional(readOnly = true)
    public CommonDto.PageResponse<AuthDto.UserSummary> listUsers(String search, int page, int size) {
        Long restauranteId = authContextService.getRestauranteId();
        String filter = search == null ? "" : search.trim();
        var users = userRepository.findByRestauranteIdAndNomeContainingIgnoreCaseOrRestauranteIdAndEmailContainingIgnoreCase(
                restauranteId,
                filter,
                restauranteId,
                filter,
                PageRequest.of(page, size)
        ).map(this::toSummary);
        return pageMapperService.toPageResponse(users);
    }

    @Transactional
    public AuthDto.UserSummary createUser(AuthDto.CreateUserRequest request) {
        credentialPolicyService.validatePasswordStrength(request.password());

        if (userRepository.existsByEmailIgnoreCase(request.email())) {
            throw new BusinessException("E-mail já cadastrado.");
        }

        User user = User.builder()
                .restaurante(authContextService.getCurrentRestaurante())
                .nome(request.nome())
                .email(request.email().trim().toLowerCase(Locale.ROOT))
                .password(passwordEncoder.encode(request.password()))
                .ativo(true)
                .roles(resolveRoles(request.roles()))
                .build();

        return toSummary(userRepository.save(user));
    }

    @Transactional
    public AuthDto.UserSummary updateUser(Long id, AuthDto.UpdateUserRequest request) {
        User user = userRepository.findByIdAndRestauranteId(id, authContextService.getRestauranteId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        userRepository.findByEmailIgnoreCase(request.email())
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new BusinessException("E-mail já cadastrado.");
                });

        user.setNome(request.nome());
        user.setEmail(request.email().trim().toLowerCase(Locale.ROOT));
        user.setAtivo(request.ativo());
        user.setRoles(resolveRoles(request.roles()));

        if (request.password() != null && !request.password().isBlank()) {
            credentialPolicyService.validatePasswordStrength(request.password());
            user.setPassword(passwordEncoder.encode(request.password()));
        }

        return toSummary(userRepository.save(user));
    }

    private Set<Role> resolveRoles(Set<RoleName> roleNames) {
        return roleNames.stream()
                .map(this::resolveRole)
                .collect(Collectors.toSet());
    }

    private Role resolveRole(RoleName roleName) {
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new BusinessException("Perfil " + roleName + " não encontrado."));
    }

    private AuthDto.UserSummary toSummary(User user) {
        return new AuthDto.UserSummary(
                user.getId(),
                user.getNome(),
                user.getEmail(),
                user.getRestaurante().getNome(),
                user.getRestaurante().getId(),
                user.getRoles().stream().map(Role::getName).collect(Collectors.toSet())
        );
    }
}
