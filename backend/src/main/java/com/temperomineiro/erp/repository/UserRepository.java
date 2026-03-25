package com.temperomineiro.erp.repository;

import com.temperomineiro.erp.model.User;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    @EntityGraph(attributePaths = {"roles", "restaurante"})
    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    @EntityGraph(attributePaths = {"roles"})
    Page<User> findByRestauranteIdAndNomeContainingIgnoreCaseOrRestauranteIdAndEmailContainingIgnoreCase(
            Long restauranteId,
            String nome,
            Long sameRestauranteId,
            String email,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"roles"})
    Optional<User> findByIdAndRestauranteId(Long id, Long restauranteId);
}

