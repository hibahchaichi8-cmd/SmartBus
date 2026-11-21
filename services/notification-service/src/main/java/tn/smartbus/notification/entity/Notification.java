package tn.smartbus.notification.entity;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "notifications")
@CompoundIndex(
        name = "unique_event_recipient",
        def = "{'eventId': 1, 'recipientId': 1}",
        unique = true
)
@CompoundIndex(
        name = "recipient_created",
        def = "{'recipientId': 1, 'createdAt': -1}"
)
public record Notification(

        @Id
        String id,

        @Version
        Long version,

        String eventId,

        String recipientId,

        NotificationType type,

        String tripId,

        String message,

        Instant createdAt,

        Instant readAt

) {
}