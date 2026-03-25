package com.temperomineiro.erp.service;

import com.temperomineiro.erp.dto.PublicDto;
import com.temperomineiro.erp.model.Produto;
import com.temperomineiro.erp.repository.ProdutoRepository;
import java.text.Normalizer;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PublicMenuService {

    private final MesaService mesaService;
    private final ProdutoRepository produtoRepository;
    private final PedidoService pedidoService;

    @Transactional(readOnly = true)
    public PublicDto.PublicMenuResponse getMenu(String restaurantSlug, String mesaToken) {
        var mesa = mesaService.getByPublicTokenAndRestaurantSlug(mesaToken, restaurantSlug);
        List<Produto> produtos = produtoRepository.findByRestauranteSlugIgnoreCaseAndDisponivelTrueOrderByCategoriaOrdemExibicaoAscNomeAsc(restaurantSlug)
                .stream()
                .sorted(Comparator
                        .comparingInt((Produto produto) -> categoryPriority(produto.getCategoria().getNome()))
                        .thenComparing(produto -> produto.getCategoria().getOrdemExibicao())
                        .thenComparing(Produto::getNome, String.CASE_INSENSITIVE_ORDER))
                .toList();

        Map<Long, PublicDto.PublicCategoryResponse> grouped = new LinkedHashMap<>();
        for (Produto produto : produtos) {
            grouped.computeIfAbsent(produto.getCategoria().getId(), key -> new PublicDto.PublicCategoryResponse(
                    produto.getCategoria().getId(),
                    produto.getCategoria().getNome(),
                    produto.getCategoria().getDescricao(),
                    new java.util.ArrayList<>()
            ));

            @SuppressWarnings("unchecked")
            List<PublicDto.PublicProductResponse> items = (List<PublicDto.PublicProductResponse>) grouped.get(produto.getCategoria().getId()).produtos();
            items.add(new PublicDto.PublicProductResponse(
                    produto.getId(),
                    produto.getNome(),
                    produto.getDescricao(),
                    produto.getPreco(),
                    produto.getImagemUrl()
            ));
        }

        return new PublicDto.PublicMenuResponse(
                mesa.getRestaurante().getNome(),
                mesa.getNome(),
                mesa.getPublicToken(),
                grouped.values().stream().toList()
        );
    }

    @Transactional
    public com.temperomineiro.erp.dto.PedidoDto.PedidoResponse createPublicOrder(String restaurantSlug, PublicDto.PublicOrderRequest request) {
        return pedidoService.createPublicOrder(restaurantSlug, request);
    }

    private int categoryPriority(String categoryName) {
        String normalized = normalize(categoryName);
        if (normalized.contains("prato")) {
            return 0;
        }
        if (normalized.contains("bebida")) {
            return 1;
        }
        if (normalized.contains("sobremesa") || normalized.contains("doce")) {
            return 2;
        }
        return 10;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }

        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "").toLowerCase(Locale.ROOT);
    }
}
