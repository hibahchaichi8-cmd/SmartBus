package tn.smartbus.trip.entity;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "pending_notification_events")
public record TripNotificationEvent(

        @Id
        String id,

        String recipientId,

        TripNotificationType type,

        String tripId,

        String message,

        @Indexed
        Instant occurredAt

) {
}