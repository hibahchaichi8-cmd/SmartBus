package tn.smartbus.trip.dto;

import java.time.Instant;

public record PendingNotificationEvent(
        String eventId,
        String recipientId,
        String type,
        String tripId,
        String message,
        Instant occurredAt
) {
}