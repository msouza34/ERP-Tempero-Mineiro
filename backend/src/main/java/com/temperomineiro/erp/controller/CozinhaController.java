package com.temperomineiro.erp.controller;

import com.temperomineiro.erp.dto.PedidoDto;
import com.temperomineiro.erp.service.AuthContextService;
import com.temperomineiro.erp.service.NotificationService;
import com.temperomineiro.erp.service.PedidoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/cozinha")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','GERENTE','COZINHA')")
@Tag(name = "Cozinha")
public class CozinhaController {

    private final PedidoService pedidoService;
    private final NotificationService notificationService;
    private final AuthContextService authContextService;

    @GetMapping("/pedidos")
    public List<PedidoDto.PedidoResponse> getKitchenOrders() {
        return pedidoService.getKitchenOrders();
    }

    @GetMapping("/stream")
    public SseEmitter stream() {
        return notificationService.subscribe(authContextService.getRestauranteId());
    }
}
