package com.yaxinaz.notification.dto;

import java.time.Instant;

public record NotificationResponse(
        Long id,
        String type,
        String title,
        String message,
        Long referenceId,
        boolean read,
        Instant createdAt
) {
}
