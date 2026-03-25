package com.temperomineiro.erp.controller;

import com.temperomineiro.erp.dto.CommonDto;
import com.temperomineiro.erp.dto.MesaDto;
import com.temperomineiro.erp.service.MesaService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/mesas")
@RequiredArgsConstructor
@Tag(name = "Mesas")
public class MesaController {

    private final MesaService mesaService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','GARCOM','CAIXA')")
    public CommonDto.PageResponse<MesaDto.MesaResponse> list(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        return mesaService.list(search, page, size);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    public MesaDto.MesaResponse create(@Valid @RequestBody MesaDto.MesaRequest request) {
        return mesaService.create(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
    public MesaDto.MesaResponse update(@PathVariable Long id, @Valid @RequestBody MesaDto.MesaRequest request) {
        return mesaService.update(id, request);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','GARCOM','CAIXA')")
    public MesaDto.MesaResponse updateStatus(@PathVariable Long id, @Valid @RequestBody MesaDto.MesaStatusRequest request) {
        return mesaService.updateStatus(id, request);
    }

    @PostMapping("/{id}/abrir")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','GARCOM')")
    public MesaDto.MesaResponse open(@PathVariable Long id) {
        return mesaService.abrirMesa(id);
    }

    @PostMapping("/{id}/fechar")
    @PreAuthorize("hasAnyRole('ADMIN','GERENTE','CAIXA')")
    public MesaDto.MesaResponse close(@PathVariable Long id) {
        return mesaService.fecharMesaManualmente(id);
    }
}
