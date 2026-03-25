package com.temperomineiro.erp.repository;

import com.temperomineiro.erp.model.Produto;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProdutoRepository extends JpaRepository<Produto, Long> {

    @EntityGraph(attributePaths = {"categoria"})
    Page<Produto> findByRestauranteIdAndNomeContainingIgnoreCaseOrderByCategoriaOrdemExibicaoAscNomeAsc(
            Long restauranteId,
            String nome,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"categoria"})
    Page<Produto> findByRestauranteIdAndCategoriaIdAndNomeContainingIgnoreCaseOrderByNomeAsc(
            Long restauranteId,
            Long categoriaId,
            String nome,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"categoria"})
    Optional<Produto> findByIdAndRestauranteId(Long id, Long restauranteId);

    @EntityGraph(attributePaths = {"categoria"})
    List<Produto> findByRestauranteSlugIgnoreCaseAndDisponivelTrueOrderByCategoriaOrdemExibicaoAscNomeAsc(String slug);
}
