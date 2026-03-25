package com.temperomineiro.erp.repository;

import com.temperomineiro.erp.model.Pagamento;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {

    List<Pagamento> findByRestauranteIdAndMesaIdOrderByPagoEmDesc(Long restauranteId, Long mesaId);

    @Query("""
            select coalesce(sum(p.valor), 0)
            from Pagamento p
            where p.restaurante.id = :restauranteId
              and p.mesa.id = :mesaId
              and p.status = com.temperomineiro.erp.model.DomainEnums$PaymentStatus.CONCLUIDO
            """)
    BigDecimal totalPagoPorMesa(@Param("restauranteId") Long restauranteId, @Param("mesaId") Long mesaId);
}

