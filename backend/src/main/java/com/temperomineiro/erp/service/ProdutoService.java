package com.temperomineiro.erp.service;

import com.temperomineiro.erp.dto.CatalogDto;
import com.temperomineiro.erp.dto.CommonDto;
import com.temperomineiro.erp.exception.ResourceNotFoundException;
import com.temperomineiro.erp.model.Estoque;
import com.temperomineiro.erp.model.Produto;
import com.temperomineiro.erp.model.ReceitaProduto;
import com.temperomineiro.erp.repository.ProdutoRepository;
import com.temperomineiro.erp.repository.ReceitaProdutoRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;
    private final ReceitaProdutoRepository receitaProdutoRepository;
    private final CategoriaService categoriaService;
    private final EstoqueService estoqueService;
    private final AuthContextService authContextService;
    private final PageMapperService pageMapperService;
    private final ProductImageService productImageService;

    @Transactional(readOnly = true)
    public CommonDto.PageResponse<CatalogDto.ProdutoResponse> list(String search, Long categoriaId, int page, int size) {
        String filter = search == null ? "" : search.trim();
        var pageResult = categoriaId == null
                ? produtoRepository.findByRestauranteIdAndNomeContainingIgnoreCaseOrderByCategoriaOrdemExibicaoAscNomeAsc(
                        authContextService.getRestauranteId(),
                        filter,
                        PageRequest.of(page, size)
                )
                : produtoRepository.findByRestauranteIdAndCategoriaIdAndNomeContainingIgnoreCaseOrderByNomeAsc(
                        authContextService.getRestauranteId(),
                        categoriaId,
                        filter,
                        PageRequest.of(page, size)
                );

        return pageMapperService.toPageResponse(pageResult.map(this::toResponse));
    }

    @Transactional
    public CatalogDto.ProdutoResponse create(CatalogDto.ProdutoRequest request) {
        Produto produto = Produto.builder()
                .restaurante(authContextService.getCurrentRestaurante())
                .sku(generateSku())
                .build();
        fillAndSave(produto, request);
        return toResponse(produto);
    }

    @Transactional
    public CatalogDto.ProdutoResponse update(Long id, CatalogDto.ProdutoRequest request) {
        Produto produto = getEntity(id);
        fillAndSave(produto, request);
        return toResponse(produto);
    }

    @Transactional
    public void delete(Long id) {
        Produto produto = getEntity(id);
        produto.setDisponivel(false);
        produtoRepository.save(produto);
    }

    @Transactional(readOnly = true)
    public Produto getEntity(Long id) {
        return produtoRepository.findByIdAndRestauranteId(id, authContextService.getRestauranteId())
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado."));
    }

    @Transactional(readOnly = true)
    public Produto getEntity(Long id, Long restauranteId) {
        return produtoRepository.findByIdAndRestauranteId(id, restauranteId)
                .orElseThrow(() -> new ResourceNotFoundException("Produto não encontrado."));
    }

    @Transactional(readOnly = true)
    public List<CatalogDto.ProdutoResponse> listPublicProducts(String restaurantSlug) {
        return produtoRepository.findByRestauranteSlugIgnoreCaseAndDisponivelTrueOrderByCategoriaOrdemExibicaoAscNomeAsc(restaurantSlug)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private void fillAndSave(Produto produto, CatalogDto.ProdutoRequest request) {
        produto.setCategoria(categoriaService.getEntity(request.categoriaId()));
        produto.setNome(request.nome());
        produto.setDescricao(request.descricao());
        produto.setPreco(request.preco());
        produto.setImagemUrl(productImageService.normalize(request.imagemUrl()));
        produto.setDisponivel(request.disponivel());
        Produto savedProduct = produtoRepository.save(produto);
        saveRecipe(savedProduct, request.receita());
    }

    private void saveRecipe(Produto produto, List<CatalogDto.ReceitaItemRequest> receita) {
        receitaProdutoRepository.deleteByProdutoId(produto.getId());
        if (receita == null || receita.isEmpty()) {
            return;
        }

        for (CatalogDto.ReceitaItemRequest item : receita) {
            Estoque estoque = estoqueService.getEntity(item.estoqueId());
            receitaProdutoRepository.save(ReceitaProduto.builder()
                    .produto(produto)
                    .estoque(estoque)
                    .quantidadeConsumida(item.quantidadeConsumida())
                    .build());
        }
    }

    private CatalogDto.ProdutoResponse toResponse(Produto produto) {
        List<CatalogDto.ReceitaItemResponse> recipe = receitaProdutoRepository.findByProdutoId(produto.getId())
                .stream()
                .map(item -> new CatalogDto.ReceitaItemResponse(
                        item.getId(),
                        item.getEstoque().getId(),
                        item.getEstoque().getNome(),
                        item.getQuantidadeConsumida()
                ))
                .toList();

        return new CatalogDto.ProdutoResponse(
                produto.getId(),
                produto.getCategoria().getId(),
                produto.getCategoria().getNome(),
                produto.getNome(),
                produto.getDescricao(),
                produto.getPreco(),
                produto.getImagemUrl(),
                produto.isDisponivel(),
                produto.getSku(),
                recipe
        );
    }

    private String generateSku() {
        return "TM-" + authContextService.getRestauranteId() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
