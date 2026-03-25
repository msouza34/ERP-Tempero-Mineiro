package com.temperomineiro.erp.controller;

import com.temperomineiro.erp.dto.CatalogDto;
import com.temperomineiro.erp.dto.CommonDto;
import com.temperomineiro.erp.service.CategoriaService;
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
@RequestMapping("/categorias")
@RequiredArgsConstructor
@Tag(name = "Categorias")
public class CategoriaController {

    private final CategoriaService categoriaService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','GARCOM','COZINHA','CAIXA')")
    public CommonDto.PageResponse<CatalogDto.CategoriaResponse> list(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return categoriaService.list(search, page, size);
    }

    @GetMapping("/ativas")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','GARCOM','COZINHA','CAIXA')")
    public java.util.List<CatalogDto.CategoriaResponse> active() {
        return categoriaService.listActive();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    public CatalogDto.CategoriaResponse create(@Valid @RequestBody CatalogDto.CategoriaRequest request) {
        return categoriaService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    public CatalogDto.CategoriaResponse update(@PathVariable Long id, @Valid @RequestBody CatalogDto.CategoriaRequest request) {
        return categoriaService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    public void delete(@PathVariable Long id) {
        categoriaService.delete(id);
    }
}
