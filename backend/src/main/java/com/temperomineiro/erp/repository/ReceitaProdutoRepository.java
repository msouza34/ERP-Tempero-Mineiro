package com.temperomineiro.erp.repository;

import com.temperomineiro.erp.model.ReceitaProduto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReceitaProdutoRepository extends JpaRepository<ReceitaProduto, Long> {

    List<ReceitaProduto> findByProdutoId(Long produtoId);

    void deleteByProdutoId(Long produtoId);
}

