package tn.smartbus.notification.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import tn.smartbus.notification.dto.CreateNotificationRequest;
import tn.smartbus.notification.dto.PendingTripEvent;
import tn.smartbus.notification.entity.NotificationType;

@Component
public class NotificationEventProcessor {

    private static final Logger log =
            LoggerFactory.getLogger(NotificationEventProcessor.class);

    private final NotificationSourceClient source;
    private final NotificationService notifications;

    public NotificationEventProcessor(
            NotificationSourceClient source,
            NotificationService notifications) {

        this.source = source;
        this.notifications = notifications;
    }

    @Scheduled(
            fixedDelayString = "${app.notification-poll-ms:15000}",
            initialDelayString = "10000"
    )
    public void processPendingEvents() {

        PendingTripEvent[] events;

        try {
            events = source.getPendingEvents();

        } catch (RuntimeException exception) {

            log.warn(
                    "Cannot fetch notification events. Will retry: {}",
                    exception.getClass().getSimpleName()
            );

            return;
        }

        for (PendingTripEvent event : events) {

            if (event == null) {
                continue;
            }

            try {
                process(event);

            } catch (RuntimeException exception) {

                log.warn(
                        "Notification event {} remains pending: {}",
                        event.eventId(),
                        exception.getClass().getSimpleName()
                );
            }
        }
    }

    private void process(PendingTripEvent event) {

        NotificationType type =
                NotificationType.valueOf(event.type());

        List<String> recipients;

        if (type == NotificationType.TRIP_ASSIGNED
                || type == NotificationType.TRIP_COMPLETED) {

            if (event.recipientId() == null
                    || event.recipientId().isBlank()) {

                throw new IllegalStateException(
                        "Driver recipient is missing"
                );
            }

            recipients = List.of(event.recipientId());

        } else {

            recipients = source.getCitizenRecipients(event);
        }

        for (String recipientId : recipients) {

            if (recipientId == null || recipientId.isBlank()) {
                throw new IllegalStateException(
                        "Invalid notification recipient"
                );
            }

            notifications.create(
                    new CreateNotificationRequest(
                            event.eventId(),
                            recipientId,
                            type,
                            event.tripId(),
                            event.message()
                    )
            );
        }

        // Confirm only after all notifications have been stored.
        source.acknowledge(event.eventId());
    }
}