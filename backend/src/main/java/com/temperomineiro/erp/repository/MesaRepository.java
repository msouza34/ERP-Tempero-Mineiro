package com.temperomineiro.erp.repository;

import com.temperomineiro.erp.model.DomainEnums.MesaStatus;
import com.temperomineiro.erp.model.Mesa;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MesaRepository extends JpaRepository<Mesa, Long> {

    Page<Mesa> findByRestauranteIdAndNomeContainingIgnoreCase(Long restauranteId, String nome, Pageable pageable);

    List<Mesa> findByRestauranteIdAndStatus(Long restauranteId, MesaStatus status);

    long countByRestauranteIdAndStatus(Long restauranteId, MesaStatus status);

    Optional<Mesa> findByIdAndRestauranteId(Long id, Long restauranteId);

    Optional<Mesa> findByPublicTokenAndRestauranteSlugIgnoreCase(String publicToken, String slug);
}
