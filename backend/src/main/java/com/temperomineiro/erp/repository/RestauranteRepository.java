package com.temperomineiro.erp.repository;

import com.temperomineiro.erp.model.Restaurante;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestauranteRepository extends JpaRepository<Restaurante, Long> {

    Optional<Restaurante> findBySlugIgnoreCase(String slug);

    boolean existsBySlugIgnoreCase(String slug);
}

