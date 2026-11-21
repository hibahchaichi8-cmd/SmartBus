package tn.smartbus.notification.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PendingTripEvent(
        String eventId,
        String recipientId,
        String type,
        String tripId,
        String message,
        Instant occurredAt
) {
}