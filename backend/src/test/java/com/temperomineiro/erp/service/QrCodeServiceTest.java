package com.temperomineiro.erp.service;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class QrCodeServiceTest {

    private final QrCodeService qrCodeService = new QrCodeService();

    @Test
    void shouldGenerateQrCodeImage() {
        byte[] qrCode = qrCodeService.generateQrCode("https://api.temperomineiro.com/public/tempero-mineiro/menu?mesaToken=mesa-01");
        assertTrue(qrCode.length > 0);
    }
}
