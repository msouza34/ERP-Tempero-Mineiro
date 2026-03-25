package com.temperomineiro.erp.controller;

import com.temperomineiro.erp.service.AuthContextService;
import com.temperomineiro.erp.service.NotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN','GERENTE','GARCOM','COZINHA','CAIXA')")
@Tag(name = "Notificacoes")
public class NotificationController {

    private final NotificationService notificationService;
    private final AuthContextService authContextService;

    @GetMapping("/stream")
    public SseEmitter stream() {
        return notificationService.subscribe(authContextService.getRestauranteId());
    }
}
