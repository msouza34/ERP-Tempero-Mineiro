package com.temperomineiro.erp.dto;

import com.temperomineiro.erp.model.DomainEnums.PaymentMethod;
import com.temperomineiro.erp.model.DomainEnums.PaymentStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

public final class PaymentDto {

    private PaymentDto() {
    }

    public record RegistrarPagamentoRequest(
            @NotNull PaymentMethod metodo,
            @NotNull @DecimalMin("0.01") BigDecimal valor,
            String observacoes
    ) {
    }

    public record PagamentoResponse(
            Long id,
            PaymentMethod metodo,
            PaymentStatus status,
            BigDecimal valor,
            String observacoes,
            OffsetDateTime pagoEm
    ) {
    }

    public record CaixaResumoResponse(
            Long mesaId,
            String mesaNome,
            BigDecimal totalConsumido,
            BigDecimal totalPago,
            BigDecimal saldoPendente,
            List<PagamentoResponse> pagamentos,
            List<PedidoDto.PedidoResponse> pedidos
    ) {
    }
}

