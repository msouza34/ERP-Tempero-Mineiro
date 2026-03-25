package com.temperomineiro.erp.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public final class PublicDto {

    private PublicDto() {
    }

    public record PublicProductResponse(
            Long id,
            String nome,
            String descricao,
            java.math.BigDecimal preco,
            String imagemUrl
    ) {
    }

    public record PublicCategoryResponse(
            Long id,
            String nome,
            String descricao,
            List<PublicProductResponse> produtos
    ) {
    }

    public record PublicMenuResponse(
            String restaurante,
            String mesa,
            String mesaToken,
            List<PublicCategoryResponse> categorias
    ) {
    }

    public record PublicOrderItemRequest(
            @NotNull Long produtoId,
            @NotNull @Min(1) Integer quantidade,
            String observacoes
    ) {
    }

    public record PublicOrderRequest(
            @NotBlank String mesaToken,
            String observacoes,
            @NotEmpty @Valid List<PublicOrderItemRequest> itens
    ) {
    }
}
