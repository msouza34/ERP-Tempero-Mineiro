package com.temperomineiro.erp.service;

import com.temperomineiro.erp.exception.BusinessException;
import java.net.URI;
import java.util.Base64;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class ProductImageService {

    private static final int MAX_IMAGE_BYTES = 1_500_000;
    private static final int MAX_URL_LENGTH = 2_048;
    private static final Pattern DATA_URL_PATTERN = Pattern.compile(
            "^data:image/(png|jpeg|jpg|webp);base64,[A-Za-z0-9+/=]+$",
            Pattern.CASE_INSENSITIVE
    );
    private static final Set<String> ALLOWED_SCHEMES = Set.of("http", "https");

    public String normalize(String imageValue) {
        if (imageValue == null || imageValue.isBlank()) {
            return null;
        }

        String normalized = imageValue.trim();
        if (normalized.startsWith("data:image/")) {
            validateDataUrl(normalized);
            return normalized;
        }

        validateHttpUrl(normalized);
        return normalized;
    }

    private void validateDataUrl(String imageValue) {
        if (!DATA_URL_PATTERN.matcher(imageValue).matches()) {
            throw new BusinessException("Formato de imagem inválido. Envie PNG, JPG ou WEBP.");
        }

        String base64Content = imageValue.substring(imageValue.indexOf(',') + 1);
        byte[] decoded;
        try {
            decoded = Base64.getDecoder().decode(base64Content);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("A imagem enviada não pôde ser processada.");
        }

        if (decoded.length > MAX_IMAGE_BYTES) {
            throw new BusinessException("A imagem deve ter no máximo 1,5 MB.");
        }
    }

    private void validateHttpUrl(String imageValue) {
        if (imageValue.length() > MAX_URL_LENGTH) {
            throw new BusinessException("A URL da imagem é muito longa.");
        }

        URI uri;
        try {
            uri = URI.create(imageValue);
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("A URL da imagem é inválida.");
        }

        String scheme = uri.getScheme();
        if (scheme == null || !ALLOWED_SCHEMES.contains(scheme.toLowerCase(Locale.ROOT))) {
            throw new BusinessException("A imagem deve usar uma URL http(s) válida.");
        }

        if (uri.getHost() == null || uri.getHost().isBlank()) {
            throw new BusinessException("A URL da imagem é inválida.");
        }
    }
}
