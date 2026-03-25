package com.temperomineiro.erp.repository;

import com.temperomineiro.erp.model.MovimentacaoEstoque;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long> {

    Page<MovimentacaoEstoque> findByRestauranteIdOrderByCreatedAtDesc(Long restauranteId, Pageable pageable);
}

