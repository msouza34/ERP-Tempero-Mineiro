package com.temperomineiro.erp.dto;

import java.math.BigDecimal;
import java.util.List;

public final class ReportDto {

    private ReportDto() {
    }

    public record DashboardResponse(
            BigDecimal faturamentoDia,
            BigDecimal faturamentoMes,
            long pedidosAbertos,
            long mesasOcupadas,
            long alertasEstoqueBaixo
    ) {
    }

    public record SalesSummaryResponse(
            BigDecimal faturamento,
            BigDecimal ticketMedio
    ) {
    }

    public record TopProductResponse(
            String nome,
            Long quantidade,
            BigDecimal valor
    ) {
    }

    public record FullReportResponse(
            SalesSummaryResponse resumo,
            List<TopProductResponse> produtosMaisVendidos
    ) {
    }
}

