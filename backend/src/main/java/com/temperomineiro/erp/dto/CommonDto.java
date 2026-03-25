package com.temperomineiro.erp.dto;

import java.time.OffsetDateTime;
import java.util.List;

public final class CommonDto {

    private CommonDto() {
    }

    public record PageResponse<T>(
            List<T> content,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {
    }

    public record ApiErrorResponse(
            OffsetDateTime timestamp,
            int status,
            String error,
            String message,
            String path
    ) {
    }
}

