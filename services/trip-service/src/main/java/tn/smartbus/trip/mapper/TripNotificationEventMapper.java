package tn.smartbus.trip.mapper;

import org.springframework.stereotype.Component;

import tn.smartbus.trip.dto.PendingNotificationEvent;
import tn.smartbus.trip.entity.TripNotificationEvent;

@Component
public class TripNotificationEventMapper {

    public PendingNotificationEvent toResponse(
            TripNotificationEvent event) {

        return new PendingNotificationEvent(
                event.id(),
                event.recipientId(),
                event.type().name(),
                event.tripId(),
                event.message(),
                event.occurredAt()
        );
    }
}