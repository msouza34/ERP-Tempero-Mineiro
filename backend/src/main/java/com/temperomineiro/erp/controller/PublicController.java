package com.temperomineiro.erp.controller;

import com.temperomineiro.erp.dto.PedidoDto;
import com.temperomineiro.erp.dto.PublicDto;
import com.temperomineiro.erp.service.PublicMenuService;
import com.temperomineiro.erp.service.QrCodeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/public")
@RequiredArgsConstructor
@Tag(name = "Publico")
public class PublicController {

    private final PublicMenuService publicMenuService;
    private final QrCodeService qrCodeService;

    @GetMapping("/{restaurantSlug}/menu")
    public PublicDto.PublicMenuResponse menu(@PathVariable String restaurantSlug,
                                             @RequestParam String mesaToken) {
        return publicMenuService.getMenu(restaurantSlug, mesaToken);
    }

    @PostMapping("/{restaurantSlug}/orders")
    public PedidoDto.PedidoResponse createOrder(@PathVariable String restaurantSlug,
                                                @Valid @RequestBody PublicDto.PublicOrderRequest request) {
        return publicMenuService.createPublicOrder(restaurantSlug, request);
    }

    @GetMapping(value = "/qr", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> qrCode(@RequestParam String text) {
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .cacheControl(CacheControl.noCache())
                .body(qrCodeService.generateQrCode(text));
    }
}
