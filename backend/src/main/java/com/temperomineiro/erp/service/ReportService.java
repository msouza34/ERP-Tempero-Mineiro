package com.temperomineiro.erp.service;

import com.temperomineiro.erp.dto.ReportDto;
import com.temperomineiro.erp.model.DomainEnums.MesaStatus;
import com.temperomineiro.erp.model.DomainEnums.PedidoStatus;
import com.temperomineiro.erp.repository.EstoqueRepository;
import com.temperomineiro.erp.repository.MesaRepository;
import com.temperomineiro.erp.repository.PedidoRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {

    private static final ZoneId ZONE_ID = ZoneId.of("America/Sao_Paulo");

    private final AuthContextService authContextService;
    private final PedidoRepository pedidoRepository;
    private final MesaRepository mesaRepository;
    private final EstoqueRepository estoqueRepository;

    @Transactional(readOnly = true)
    public ReportDto.DashboardResponse getDashboard() {
        Long restauranteId = authContextService.getRestauranteId();
        OffsetDateTime startOfDay = startOfDay(LocalDate.now(ZONE_ID));
        OffsetDateTime endOfDay = endOfDay(LocalDate.now(ZONE_ID));
        LocalDate today = LocalDate.now(ZONE_ID);
        OffsetDateTime startOfMonth = startOfDay(today.withDayOfMonth(1));
        OffsetDateTime endOfMonth = endOfDay(today.withDayOfMonth(today.lengthOfMonth()));

        return new ReportDto.DashboardResponse(
                pedidoRepository.sumTotalByPeriod(restauranteId, startOfDay, endOfDay),
                pedidoRepository.sumTotalByPeriod(restauranteId, startOfMonth, endOfMonth),
                pedidoRepository.countByRestauranteIdAndStatusIn(restauranteId, java.util.List.of(PedidoStatus.EM_PREPARO, PedidoStatus.PRONTO, PedidoStatus.ENTREGUE)),
                mesaRepository.countByRestauranteIdAndStatus(restauranteId, MesaStatus.OCUPADA),
                estoqueRepository.findLowStock(restauranteId).size()
        );
    }

    @Transactional(readOnly = true)
    public ReportDto.FullReportResponse getSalesReport(LocalDate startDate, LocalDate endDate) {
        Long restauranteId = authContextService.getRestauranteId();
        LocalDate effectiveStart = startDate == null ? LocalDate.now(ZONE_ID).withDayOfMonth(1) : startDate;
        LocalDate effectiveEnd = endDate == null ? LocalDate.now(ZONE_ID) : endDate;
        OffsetDateTime inicio = startOfDay(effectiveStart);
        OffsetDateTime fim = endOfDay(effectiveEnd);

        BigDecimal faturamento = pedidoRepository.sumTotalByPeriod(restauranteId, inicio, fim);
        long totalPedidos = pedidoRepository.countClosedByPeriod(restauranteId, inicio, fim);
        BigDecimal ticketMedio = totalPedidos == 0
                ? BigDecimal.ZERO
                : faturamento.divide(BigDecimal.valueOf(totalPedidos), 2, RoundingMode.HALF_UP);

        var topProducts = pedidoRepository.topProducts(restauranteId, inicio, fim, PageRequest.of(0, 5))
                .stream()
                .map(row -> new ReportDto.TopProductResponse(
                        (String) row[0],
                        ((Number) row[1]).longValue(),
                        (BigDecimal) row[2]
                ))
                .toList();

        return new ReportDto.FullReportResponse(
                new ReportDto.SalesSummaryResponse(faturamento, ticketMedio),
                topProducts
        );
    }

    private OffsetDateTime startOfDay(LocalDate localDate) {
        return localDate.atStartOfDay(ZONE_ID).toOffsetDateTime();
    }

    private OffsetDateTime endOfDay(LocalDate localDate) {
        return localDate.plusDays(1).atStartOfDay(ZONE_ID).minusNanos(1).toOffsetDateTime();
    }
}

