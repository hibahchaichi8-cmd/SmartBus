package tn.smartbus.notification.dto;

import java.time.Instant;

import tn.smartbus.notification.entity.NotificationType;

public record NotificationResponse(
        String id,
        NotificationType type,
        String tripId,
        String message,
        Instant createdAt,
        Instant readAt
) {
}