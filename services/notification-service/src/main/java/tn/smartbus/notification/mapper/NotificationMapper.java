package tn.smartbus.notification.mapper;

import org.springframework.stereotype.Component;

import tn.smartbus.notification.dto.NotificationResponse;
import tn.smartbus.notification.entity.Notification;

@Component
public class NotificationMapper {

    public NotificationResponse toResponse(
            Notification notification) {

        return new NotificationResponse(
                notification.id(),
                notification.type(),
                notification.tripId(),
                notification.message(),
                notification.createdAt(),
                notification.readAt()
        );
    }
}