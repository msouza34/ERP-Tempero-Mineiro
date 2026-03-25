package com.temperomineiro.erp.dto;

import java.time.OffsetDateTime;

public final class NotificationDto {

    private NotificationDto() {
    }

    public record NotificationEvent(
            String type,
            String message,
            Long entityId,
            OffsetDateTime timestamp
    ) {
    }
}

