package com.temperomineiro.erp.controller;

import com.temperomineiro.erp.dto.PaymentDto;
import com.temperomineiro.erp.service.CaixaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/caixa")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','GERENTE','CAIXA')")
@Tag(name = "Caixa")
public class CaixaController {

    private final CaixaService caixaService;

    @GetMapping("/mesas/{mesaId}/resumo")
    public PaymentDto.CaixaResumoResponse resumo(@PathVariable Long mesaId) {
        return caixaService.getResumo(mesaId);
    }

    @PostMapping("/mesas/{mesaId}/pagamentos")
    public PaymentDto.PagamentoResponse payment(@PathVariable Long mesaId,
                                                @Valid @RequestBody PaymentDto.RegistrarPagamentoRequest request) {
        return caixaService.registrarPagamento(mesaId, request);
    }

    @PostMapping("/mesas/{mesaId}/fechar")
    public PaymentDto.CaixaResumoResponse close(@PathVariable Long mesaId) {
        return caixaService.fecharConta(mesaId);
    }
}
