package com.temperomineiro.erp.controller;

import com.temperomineiro.erp.dto.CatalogDto;
import com.temperomineiro.erp.dto.CommonDto;
import com.temperomineiro.erp.service.ProdutoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/produtos")
@RequiredArgsConstructor
@Tag(name = "Produtos")
public class ProdutoController {

    private final ProdutoService produtoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','GARCOM','COZINHA','CAIXA')")
    public CommonDto.PageResponse<CatalogDto.ProdutoResponse> list(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(required = false) Long categoriaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        return produtoService.list(search, categoriaId, page, size);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    public CatalogDto.ProdutoResponse create(@Valid @RequestBody CatalogDto.ProdutoRequest request) {
        return produtoService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    public CatalogDto.ProdutoResponse update(@PathVariable Long id, @Valid @RequestBody CatalogDto.ProdutoRequest request) {
        return produtoService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    public void delete(@PathVariable Long id) {
        produtoService.delete(id);
    }
}
