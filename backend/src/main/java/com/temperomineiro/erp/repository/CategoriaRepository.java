package com.temperomineiro.erp.repository;

import com.temperomineiro.erp.model.Categoria;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    Page<Categoria> findByRestauranteIdAndNomeContainingIgnoreCaseOrderByOrdemExibicaoAscNomeAsc(
            Long restauranteId,
            String nome,
            Pageable pageable
    );

    List<Categoria> findByRestauranteIdAndAtivaTrueOrderByOrdemExibicaoAsc(Long restauranteId);

    Optional<Categoria> findByIdAndRestauranteId(Long id, Long restauranteId);
}
