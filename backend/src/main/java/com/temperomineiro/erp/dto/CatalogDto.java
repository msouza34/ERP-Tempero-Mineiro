package com.temperomineiro.erp.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

public final class CatalogDto {

    private CatalogDto() {
    }

    public record CategoriaRequest(
            @NotBlank String nome,
            String descricao,
            @NotNull @Min(0) Integer ordemExibicao,
            boolean ativa
    ) {
    }

    public record CategoriaResponse(
            Long id,
            String nome,
            String descricao,
            Integer ordemExibicao,
            boolean ativa
    ) {
    }

    public record ReceitaItemRequest(
            @NotNull Long estoqueId,
            @NotNull @DecimalMin("0.001") BigDecimal quantidadeConsumida
    ) {
    }

    public record ReceitaItemResponse(
            Long id,
            Long estoqueId,
            String estoqueNome,
            BigDecimal quantidadeConsumida
    ) {
    }

    public record ProdutoRequest(
            @NotNull Long categoriaId,
            @NotBlank String nome,
            String descricao,
            @NotNull @DecimalMin("0.01") BigDecimal preco,
            String imagemUrl,
            boolean disponivel,
            @Valid List<ReceitaItemRequest> receita
    ) {
    }

    public record ProdutoResponse(
            Long id,
            Long categoriaId,
            String categoriaNome,
            String nome,
            String descricao,
            BigDecimal preco,
            String imagemUrl,
            boolean disponivel,
            String sku,
            List<ReceitaItemResponse> receita
    ) {
    }
}

