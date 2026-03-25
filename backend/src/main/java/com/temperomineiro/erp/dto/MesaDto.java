package com.temperomineiro.erp.dto;

import com.temperomineiro.erp.model.DomainEnums.MesaStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public final class MesaDto {

    private MesaDto() {
    }

    public record MesaRequest(
            @NotBlank String nome,
            @NotNull @Min(1) Integer capacidade,
            @NotNull MesaStatus status,
            boolean ativa
    ) {
    }

    public record MesaResponse(
            Long id,
            String nome,
            Integer capacidade,
            MesaStatus status,
            String publicToken,
            String qrCodeUrl,
            OffsetDateTime abertaEm,
            boolean ativa
    ) {
    }

    public record MesaStatusRequest(@NotNull MesaStatus status) {
    }
}

