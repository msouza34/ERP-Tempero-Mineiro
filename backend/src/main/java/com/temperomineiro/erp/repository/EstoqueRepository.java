package com.temperomineiro.erp.repository;

import com.temperomineiro.erp.model.Estoque;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EstoqueRepository extends JpaRepository<Estoque, Long> {

    Page<Estoque> findByRestauranteIdAndNomeContainingIgnoreCase(Long restauranteId, String nome, Pageable pageable);

    Optional<Estoque> findByIdAndRestauranteId(Long id, Long restauranteId);

    @Query("""
            select e
            from Estoque e
            where e.restaurante.id = :restauranteId
              and e.ativo = true
              and e.quantidadeAtual <= e.quantidadeMinima
            order by e.quantidadeAtual asc
            """)
    List<Estoque> findLowStock(@Param("restauranteId") Long restauranteId);
}
