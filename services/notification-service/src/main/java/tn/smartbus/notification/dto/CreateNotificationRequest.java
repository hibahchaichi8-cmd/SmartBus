package tn.smartbus.notification.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import tn.smartbus.notification.entity.NotificationType;

public record CreateNotificationRequest(

        @NotBlank(message = "Event identifier is required")
        @Size(max = 100)
        String eventId,

        @NotBlank(message = "Recipient is required")
        @Size(max = 80)
        String recipientId,

        @NotNull(message = "Notification type is required")
        NotificationType type,

        @NotBlank(message = "Trip is required")
        @Size(max = 80)
        String tripId,

        @NotBlank(message = "Message is required")
        @Size(max = 500)
        String message

) {
}