package com.temperomineiro.erp.service;

import com.temperomineiro.erp.dto.PaymentDto;
import com.temperomineiro.erp.exception.BusinessException;
import com.temperomineiro.erp.model.DomainEnums.MesaStatus;
import com.temperomineiro.erp.model.DomainEnums.PaymentStatus;
import com.temperomineiro.erp.model.DomainEnums.PedidoStatus;
import com.temperomineiro.erp.model.Mesa;
import com.temperomineiro.erp.model.Pagamento;
import com.temperomineiro.erp.model.Pedido;
import com.temperomineiro.erp.repository.PagamentoRepository;
import com.temperomineiro.erp.repository.PedidoRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CaixaService {

    private final AuthContextService authContextService;
    private final MesaService mesaService;
    private final PedidoService pedidoService;
    private final PedidoRepository pedidoRepository;
    private final PagamentoRepository pagamentoRepository;
    private final EstoqueService estoqueService;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public PaymentDto.CaixaResumoResponse getResumo(Long mesaId) {
        Mesa mesa = mesaService.getEntity(mesaId);
        Long restauranteId = authContextService.getRestauranteId();
        List<Pedido> pedidos = pedidoService.getOpenOrdersByMesa(restauranteId, mesaId)
                .stream()
                .filter(pedido -> pedido.getStatus() != PedidoStatus.CANCELADO)
                .toList();
        List<Pagamento> pagamentos = pagamentoRepository.findByRestauranteIdAndMesaIdOrderByPagoEmDesc(restauranteId, mesaId);
        return buildResumo(mesa, pedidos, pagamentos);
    }

    @Transactional
    public PaymentDto.PagamentoResponse registrarPagamento(Long mesaId, PaymentDto.RegistrarPagamentoRequest request) {
        Mesa mesa = mesaService.getEntity(mesaId);
        Pagamento pagamento = pagamentoRepository.save(Pagamento.builder()
                .restaurante(mesa.getRestaurante())
                .mesa(mesa)
                .metodo(request.metodo())
                .status(PaymentStatus.CONCLUIDO)
                .valor(request.valor())
                .observacoes(request.observacoes())
                .pagoEm(OffsetDateTime.now())
                .build());

        notificationService.publish(mesa.getRestaurante().getId(), "PAGAMENTO_REGISTRADO", "Pagamento registrado no caixa.", mesaId);
        return toResponse(pagamento);
    }

    @Transactional
    public PaymentDto.CaixaResumoResponse fecharConta(Long mesaId) {
        Mesa mesa = mesaService.getEntity(mesaId);
        Long restauranteId = authContextService.getRestauranteId();
        List<Pedido> pedidos = pedidoService.getOpenOrdersByMesa(restauranteId, mesaId)
                .stream()
                .filter(pedido -> pedido.getStatus() != PedidoStatus.CANCELADO)
                .toList();

        BigDecimal totalConsumido = pedidos.stream()
                .map(Pedido::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPago = pagamentoRepository.totalPagoPorMesa(restauranteId, mesaId);

        if (pedidos.stream().anyMatch(pedido -> pedido.getStatus() == PedidoStatus.EM_PREPARO || pedido.getStatus() == PedidoStatus.PRONTO)) {
            throw new BusinessException("Existem pedidos ainda não entregues para esta mesa.");
        }
        if (totalPago.compareTo(totalConsumido) < 0) {
            throw new BusinessException("Saldo pendente. Registre mais pagamentos para fechar a conta.");
        }

        for (Pedido pedido : pedidos) {
            estoqueService.consumeForOrder(pedido);
            pedido.setStatus(PedidoStatus.FECHADO);
            pedido.setFechadoEm(OffsetDateTime.now());
        }
        pedidoRepository.saveAll(pedidos);

        mesa.setStatus(MesaStatus.LIVRE);
        mesa.setAbertaEm(null);

        notificationService.publish(restauranteId, "CONTA_FECHADA", "Conta da mesa fechada com sucesso.", mesaId);
        List<Pagamento> pagamentos = pagamentoRepository.findByRestauranteIdAndMesaIdOrderByPagoEmDesc(restauranteId, mesaId);
        return buildResumo(mesa, pedidos, pagamentos);
    }

    private PaymentDto.CaixaResumoResponse buildResumo(Mesa mesa, List<Pedido> pedidos, List<Pagamento> pagamentos) {
        BigDecimal totalConsumido = pedidos.stream()
                .map(Pedido::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPago = pagamentos.stream()
                .filter(pagamento -> pagamento.getStatus() == PaymentStatus.CONCLUIDO)
                .map(Pagamento::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new PaymentDto.CaixaResumoResponse(
                mesa.getId(),
                mesa.getNome(),
                totalConsumido,
                totalPago,
                totalConsumido.subtract(totalPago),
                pagamentos.stream().map(this::toResponse).toList(),
                pedidos.stream().map(pedidoService::toResponse).toList()
        );
    }

    private PaymentDto.PagamentoResponse toResponse(Pagamento pagamento) {
        return new PaymentDto.PagamentoResponse(
                pagamento.getId(),
                pagamento.getMetodo(),
                pagamento.getStatus(),
                pagamento.getValor(),
                pagamento.getObservacoes(),
                pagamento.getPagoEm()
        );
    }
}
