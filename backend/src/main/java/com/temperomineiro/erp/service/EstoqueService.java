package com.temperomineiro.erp.service;

import com.temperomineiro.erp.dto.CommonDto;
import com.temperomineiro.erp.dto.EstoqueDto;
import com.temperomineiro.erp.exception.BusinessException;
import com.temperomineiro.erp.exception.ResourceNotFoundException;
import com.temperomineiro.erp.model.DomainEnums.InventoryMovementType;
import com.temperomineiro.erp.model.Estoque;
import com.temperomineiro.erp.model.ItemPedido;
import com.temperomineiro.erp.model.MovimentacaoEstoque;
import com.temperomineiro.erp.model.Pedido;
import com.temperomineiro.erp.model.ReceitaProduto;
import com.temperomineiro.erp.repository.EstoqueRepository;
import com.temperomineiro.erp.repository.MovimentacaoEstoqueRepository;
import com.temperomineiro.erp.repository.ReceitaProdutoRepository;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EstoqueService {

    private final EstoqueRepository estoqueRepository;
    private final MovimentacaoEstoqueRepository movimentacaoEstoqueRepository;
    private final ReceitaProdutoRepository receitaProdutoRepository;
    private final AuthContextService authContextService;
    private final PageMapperService pageMapperService;

    @Transactional(readOnly = true)
    public CommonDto.PageResponse<EstoqueDto.EstoqueResponse> list(String search, int page, int size) {
        String filter = search == null ? "" : search.trim();
        var result = estoqueRepository.findByRestauranteIdAndNomeContainingIgnoreCase(
                authContextService.getRestauranteId(),
                filter,
                PageRequest.of(page, size)
        ).map(this::toResponse);
        return pageMapperService.toPageResponse(result);
    }

    @Transactional(readOnly = true)
    public CommonDto.PageResponse<EstoqueDto.MovimentacaoEstoqueResponse> listMovements(int page, int size) {
        var result = movimentacaoEstoqueRepository.findByRestauranteIdOrderByCreatedAtDesc(
                authContextService.getRestauranteId(),
                PageRequest.of(page, size)
        ).map(this::toMovementResponse);
        return pageMapperService.toPageResponse(result);
    }

    @Transactional(readOnly = true)
    public List<EstoqueDto.EstoqueResponse> getLowStockItems() {
        return estoqueRepository.findLowStock(authContextService.getRestauranteId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public EstoqueDto.EstoqueResponse create(EstoqueDto.EstoqueRequest request) {
        Estoque estoque = Estoque.builder()
                .restaurante(authContextService.getCurrentRestaurante())
                .nome(request.nome())
                .unidadeMedida(request.unidadeMedida())
                .quantidadeAtual(request.quantidadeAtual())
                .quantidadeMinima(request.quantidadeMinima())
                .custoUnitario(request.custoUnitario())
                .ativo(request.ativo())
                .build();
        Estoque saved = estoqueRepository.save(estoque);
        registerMovement(saved, InventoryMovementType.ENTRADA, request.quantidadeAtual(), "Saldo inicial", "Cadastro do item");
        return toResponse(saved);
    }

    @Transactional
    public EstoqueDto.EstoqueResponse update(Long id, EstoqueDto.EstoqueRequest request) {
        Estoque estoque = getEntity(id);
        estoque.setNome(request.nome());
        estoque.setUnidadeMedida(request.unidadeMedida());
        estoque.setQuantidadeMinima(request.quantidadeMinima());
        estoque.setCustoUnitario(request.custoUnitario());
        estoque.setAtivo(request.ativo());
        if (estoque.getQuantidadeAtual().compareTo(request.quantidadeAtual()) != 0) {
            BigDecimal diff = request.quantidadeAtual().subtract(estoque.getQuantidadeAtual());
            estoque.setQuantidadeAtual(request.quantidadeAtual());
            registerMovement(
                    estoque,
                    InventoryMovementType.AJUSTE,
                    diff.abs(),
                    "Ajuste manual de cadastro",
                    "Atualização do item"
            );
        }
        return toResponse(estoqueRepository.save(estoque));
    }

    @Transactional
    public EstoqueDto.EstoqueResponse adjust(Long id, EstoqueDto.AjusteEstoqueRequest request) {
        Estoque estoque = getEntity(id);
        switch (request.tipo()) {
            case ENTRADA -> estoque.setQuantidadeAtual(estoque.getQuantidadeAtual().add(request.quantidade()));
            case SAIDA -> {
                BigDecimal remaining = estoque.getQuantidadeAtual().subtract(request.quantidade());
                if (remaining.compareTo(BigDecimal.ZERO) < 0) {
                    throw new BusinessException("Quantidade insuficiente em estoque para a saída solicitada.");
                }
                estoque.setQuantidadeAtual(remaining);
            }
            case AJUSTE -> estoque.setQuantidadeAtual(request.quantidade());
        }

        Estoque saved = estoqueRepository.save(estoque);
        registerMovement(saved, request.tipo(), request.quantidade(), request.motivo(), "Ajuste manual");
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public Estoque getEntity(Long id) {
        return estoqueRepository.findByIdAndRestauranteId(id, authContextService.getRestauranteId())
                .orElseThrow(() -> new ResourceNotFoundException("Item de estoque não encontrado."));
    }

    @Transactional(readOnly = true)
    public Estoque getEntity(Long id, Long restauranteId) {
        return estoqueRepository.findByIdAndRestauranteId(id, restauranteId)
                .orElseThrow(() -> new ResourceNotFoundException("Item de estoque não encontrado."));
    }

    @Transactional
    public void consumeForOrder(Pedido pedido) {
        for (ItemPedido itemPedido : pedido.getItens()) {
            List<ReceitaProduto> recipe = receitaProdutoRepository.findByProdutoId(itemPedido.getProduto().getId());
            for (ReceitaProduto recipeItem : recipe) {
                Estoque estoque = recipeItem.getEstoque();
                BigDecimal totalConsumption = recipeItem.getQuantidadeConsumida()
                        .multiply(BigDecimal.valueOf(itemPedido.getQuantidade()));
                BigDecimal remaining = estoque.getQuantidadeAtual().subtract(totalConsumption);
                if (remaining.compareTo(BigDecimal.ZERO) < 0) {
                    throw new BusinessException("Estoque insuficiente para o insumo " + estoque.getNome() + ".");
                }
                estoque.setQuantidadeAtual(remaining);
                estoqueRepository.save(estoque);
                registerMovement(
                        estoque,
                        InventoryMovementType.SAIDA,
                        totalConsumption,
                        "Baixa automática por venda",
                        "Pedido #" + pedido.getId()
                );
            }
        }
    }

    private void registerMovement(Estoque estoque, InventoryMovementType type, BigDecimal quantity, String reason, String reference) {
        movimentacaoEstoqueRepository.save(MovimentacaoEstoque.builder()
                .restaurante(estoque.getRestaurante())
                .estoque(estoque)
                .tipo(type)
                .quantidade(quantity)
                .motivo(reason)
                .referencia(reference)
                .build());
    }

    private EstoqueDto.EstoqueResponse toResponse(Estoque estoque) {
        return new EstoqueDto.EstoqueResponse(
                estoque.getId(),
                estoque.getNome(),
                estoque.getUnidadeMedida(),
                estoque.getQuantidadeAtual(),
                estoque.getQuantidadeMinima(),
                estoque.getCustoUnitario(),
                estoque.isAtivo(),
                estoque.getQuantidadeAtual().compareTo(estoque.getQuantidadeMinima()) <= 0
        );
    }

    private EstoqueDto.MovimentacaoEstoqueResponse toMovementResponse(MovimentacaoEstoque movement) {
        return new EstoqueDto.MovimentacaoEstoqueResponse(
                movement.getId(),
                movement.getEstoque().getId(),
                movement.getEstoque().getNome(),
                movement.getTipo(),
                movement.getQuantidade(),
                movement.getMotivo(),
                movement.getReferencia(),
                movement.getCreatedAt()
        );
    }
}

