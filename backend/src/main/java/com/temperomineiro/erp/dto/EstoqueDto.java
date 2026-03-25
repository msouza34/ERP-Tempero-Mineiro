package com.temperomineiro.erp.dto;

import com.temperomineiro.erp.model.DomainEnums.InventoryMovementType;
import com.temperomineiro.erp.model.DomainEnums.UnitMeasure;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public final class EstoqueDto {

    private EstoqueDto() {
    }

    public record EstoqueRequest(
            @NotBlank String nome,
            @NotNull UnitMeasure unidadeMedida,
            @NotNull @DecimalMin("0.000") BigDecimal quantidadeAtual,
            @NotNull @DecimalMin("0.000") BigDecimal quantidadeMinima,
            @NotNull @DecimalMin("0.00") BigDecimal custoUnitario,
            boolean ativo
    ) {
    }

    public record AjusteEstoqueRequest(
            @NotNull InventoryMovementType tipo,
            @NotNull @DecimalMin("0.001") BigDecimal quantidade,
            @NotBlank String motivo
    ) {
    }

    public record EstoqueResponse(
            Long id,
            String nome,
            UnitMeasure unidadeMedida,
            BigDecimal quantidadeAtual,
            BigDecimal quantidadeMinima,
            BigDecimal custoUnitario,
            boolean ativo,
            boolean estoqueBaixo
    ) {
    }

    public record MovimentacaoEstoqueResponse(
            Long id,
            Long estoqueId,
            String estoqueNome,
            InventoryMovementType tipo,
            BigDecimal quantidade,
            String motivo,
            String referencia,
            OffsetDateTime createdAt
    ) {
    }
}

