package com.temperomineiro.erp.controller;

import com.temperomineiro.erp.dto.ReportDto;
import com.temperomineiro.erp.service.ReportService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
@Tag(name = "Relatorios")
public class RelatorioController {

    private final ReportService reportService;

    @GetMapping("/dashboard")
    public ReportDto.DashboardResponse dashboard() {
        return reportService.getDashboard();
    }

    @GetMapping("/relatorios/vendas")
    public ReportDto.FullReportResponse salesReport(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        return reportService.getSalesReport(startDate, endDate);
    }
}
