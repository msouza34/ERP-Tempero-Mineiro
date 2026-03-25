package com.temperomineiro.erp.service;

import com.temperomineiro.erp.dto.CatalogDto;
import com.temperomineiro.erp.dto.CommonDto;
import com.temperomineiro.erp.exception.ResourceNotFoundException;
import com.temperomineiro.erp.model.Categoria;
import com.temperomineiro.erp.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final AuthContextService authContextService;
    private final PageMapperService pageMapperService;

    @Transactional(readOnly = true)
    public CommonDto.PageResponse<CatalogDto.CategoriaResponse> list(String search, int page, int size) {
        String filter = search == null ? "" : search.trim();
        var result = categoriaRepository.findByRestauranteIdAndNomeContainingIgnoreCaseOrderByOrdemExibicaoAscNomeAsc(
                authContextService.getRestauranteId(),
                filter,
                PageRequest.of(page, size)
        ).map(this::toResponse);
        return pageMapperService.toPageResponse(result);
    }

    @Transactional(readOnly = true)
    public java.util.List<CatalogDto.CategoriaResponse> listActive() {
        return categoriaRepository.findByRestauranteIdAndAtivaTrueOrderByOrdemExibicaoAsc(authContextService.getRestauranteId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public CatalogDto.CategoriaResponse create(CatalogDto.CategoriaRequest request) {
        Categoria categoria = Categoria.builder()
                .restaurante(authContextService.getCurrentRestaurante())
                .nome(request.nome())
                .descricao(request.descricao())
                .ordemExibicao(request.ordemExibicao())
                .ativa(request.ativa())
                .build();
        return toResponse(categoriaRepository.save(categoria));
    }

    @Transactional
    public CatalogDto.CategoriaResponse update(Long id, CatalogDto.CategoriaRequest request) {
        Categoria categoria = getEntity(id);
        categoria.setNome(request.nome());
        categoria.setDescricao(request.descricao());
        categoria.setOrdemExibicao(request.ordemExibicao());
        categoria.setAtiva(request.ativa());
        return toResponse(categoriaRepository.save(categoria));
    }

    @Transactional
    public void delete(Long id) {
        Categoria categoria = getEntity(id);
        categoria.setAtiva(false);
        categoriaRepository.save(categoria);
    }

    @Transactional(readOnly = true)
    public Categoria getEntity(Long id) {
        return categoriaRepository.findByIdAndRestauranteId(id, authContextService.getRestauranteId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoria não encontrada."));
    }

    private CatalogDto.CategoriaResponse toResponse(Categoria categoria) {
        return new CatalogDto.CategoriaResponse(
                categoria.getId(),
                categoria.getNome(),
                categoria.getDescricao(),
                categoria.getOrdemExibicao(),
                categoria.isAtiva()
        );
    }
}
