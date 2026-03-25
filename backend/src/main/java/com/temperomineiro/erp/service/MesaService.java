package com.temperomineiro.erp.service;

import com.temperomineiro.erp.dto.CommonDto;
import com.temperomineiro.erp.dto.MesaDto;
import com.temperomineiro.erp.exception.ResourceNotFoundException;
import com.temperomineiro.erp.model.DomainEnums.MesaStatus;
import com.temperomineiro.erp.model.Mesa;
import com.temperomineiro.erp.repository.MesaRepository;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MesaService {

    private final MesaRepository mesaRepository;
    private final AuthContextService authContextService;
    private final PageMapperService pageMapperService;

    @Value("${app.public-base-url}")
    private String publicBaseUrl;

    @Transactional(readOnly = true)
    public CommonDto.PageResponse<MesaDto.MesaResponse> list(String search, int page, int size) {
        String filter = search == null ? "" : search.trim();
        var result = mesaRepository.findByRestauranteIdAndNomeContainingIgnoreCase(
                authContextService.getRestauranteId(),
                filter,
                PageRequest.of(page, size)
        ).map(this::toResponse);
        return pageMapperService.toPageResponse(result);
    }

    @Transactional
    public MesaDto.MesaResponse create(MesaDto.MesaRequest request) {
        Mesa mesa = Mesa.builder()
                .restaurante(authContextService.getCurrentRestaurante())
                .nome(request.nome())
                .capacidade(request.capacidade())
                .status(request.status())
                .publicToken(UUID.randomUUID().toString().replace("-", ""))
                .abertaEm(request.status() == MesaStatus.OCUPADA ? OffsetDateTime.now() : null)
                .ativa(request.ativa())
                .build();
        return toResponse(mesaRepository.save(mesa));
    }

    @Transactional
    public MesaDto.MesaResponse update(Long id, MesaDto.MesaRequest request) {
        Mesa mesa = getEntity(id);
        mesa.setNome(request.nome());
        mesa.setCapacidade(request.capacidade());
        mesa.setStatus(request.status());
        mesa.setAtiva(request.ativa());
        if (request.status() == MesaStatus.OCUPADA && mesa.getAbertaEm() == null) {
            mesa.setAbertaEm(OffsetDateTime.now());
        }
        if (request.status() == MesaStatus.LIVRE) {
            mesa.setAbertaEm(null);
        }
        return toResponse(mesaRepository.save(mesa));
    }

    @Transactional
    public MesaDto.MesaResponse updateStatus(Long id, MesaDto.MesaStatusRequest request) {
        Mesa mesa = getEntity(id);
        mesa.setStatus(request.status());
        if (request.status() == MesaStatus.OCUPADA && mesa.getAbertaEm() == null) {
            mesa.setAbertaEm(OffsetDateTime.now());
        }
        if (request.status() == MesaStatus.LIVRE) {
            mesa.setAbertaEm(null);
        }
        return toResponse(mesaRepository.save(mesa));
    }

    @Transactional
    public MesaDto.MesaResponse abrirMesa(Long id) {
        Mesa mesa = getEntity(id);
        mesa.setStatus(MesaStatus.OCUPADA);
        mesa.setAbertaEm(OffsetDateTime.now());
        return toResponse(mesaRepository.save(mesa));
    }

    @Transactional
    public MesaDto.MesaResponse fecharMesaManualmente(Long id) {
        Mesa mesa = getEntity(id);
        mesa.setStatus(MesaStatus.LIVRE);
        mesa.setAbertaEm(null);
        return toResponse(mesaRepository.save(mesa));
    }

    @Transactional(readOnly = true)
    public Mesa getEntity(Long id) {
        return mesaRepository.findByIdAndRestauranteId(id, authContextService.getRestauranteId())
                .orElseThrow(() -> new ResourceNotFoundException("Mesa não encontrada."));
    }

    @Transactional(readOnly = true)
    public Mesa getByPublicTokenAndRestaurantSlug(String token, String slug) {
        return mesaRepository.findByPublicTokenAndRestauranteSlugIgnoreCase(token, slug)
                .orElseThrow(() -> new ResourceNotFoundException("Mesa pública não encontrada."));
    }

    public MesaDto.MesaResponse toResponse(Mesa mesa) {
        String publicUrl = publicBaseUrl
                + "/public/"
                + mesa.getRestaurante().getSlug()
                + "/menu?mesaToken="
                + mesa.getPublicToken();
        String qrCodeUrl = "/public/qr?text=" + URLEncoder.encode(publicUrl, StandardCharsets.UTF_8);
        return new MesaDto.MesaResponse(
                mesa.getId(),
                mesa.getNome(),
                mesa.getCapacidade(),
                mesa.getStatus(),
                mesa.getPublicToken(),
                qrCodeUrl,
                mesa.getAbertaEm(),
                mesa.isAtiva()
        );
    }
}
