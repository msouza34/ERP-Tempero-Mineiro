package com.temperomineiro.erp.controller;

import com.temperomineiro.erp.dto.CommonDto;
import com.temperomineiro.erp.dto.EstoqueDto;
import com.temperomineiro.erp.service.EstoqueService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/estoque")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
@Tag(name = "Estoque")
public class EstoqueController {

    private final EstoqueService estoqueService;

    @GetMapping
    public CommonDto.PageResponse<EstoqueDto.EstoqueResponse> list(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return estoqueService.list(search, page, size);
    }

    @GetMapping("/movimentacoes")
    public CommonDto.PageResponse<EstoqueDto.MovimentacaoEstoqueResponse> movements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return estoqueService.listMovements(page, size);
    }

    @GetMapping("/baixo")
    public List<EstoqueDto.EstoqueResponse> lowStock() {
        return estoqueService.getLowStockItems();
    }

    @PostMapping
    public EstoqueDto.EstoqueResponse create(@Valid @RequestBody EstoqueDto.EstoqueRequest request) {
        return estoqueService.create(request);
    }

    @PutMapping("/{id}")
    public EstoqueDto.EstoqueResponse update(@PathVariable Long id, @Valid @RequestBody EstoqueDto.EstoqueRequest request) {
        return estoqueService.update(id, request);
    }

    @PostMapping("/{id}/ajustes")
    public EstoqueDto.EstoqueResponse adjust(@PathVariable Long id, @Valid @RequestBody EstoqueDto.AjusteEstoqueRequest request) {
        return estoqueService.adjust(id, request);
    }
}
