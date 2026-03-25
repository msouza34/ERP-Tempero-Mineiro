package com.temperomineiro.erp.service;

import com.temperomineiro.erp.exception.BusinessException;
import com.temperomineiro.erp.model.Restaurante;
import com.temperomineiro.erp.model.User;
import com.temperomineiro.erp.repository.RestauranteRepository;
import com.temperomineiro.erp.repository.UserRepository;
import com.temperomineiro.erp.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthContextService {

    private final UserRepository userRepository;
    private final RestauranteRepository restauranteRepository;

    public CustomUserDetails getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            throw new BusinessException("Usuário autenticado não encontrado.");
        }
        return userDetails;
    }

    public Long getRestauranteId() {
        return getCurrentUserDetails().getRestauranteId();
    }

    public User getCurrentUserEntity() {
        return userRepository.findByIdAndRestauranteId(getCurrentUserDetails().getId(), getRestauranteId())
                .orElseThrow(() -> new BusinessException("Usuário autenticado não encontrado."));
    }

    public Restaurante getCurrentRestaurante() {
        return restauranteRepository.findById(getRestauranteId())
                .orElseThrow(() -> new BusinessException("Restaurante não encontrado."));
    }
}

