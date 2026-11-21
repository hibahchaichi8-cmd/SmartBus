package tn.smartbus.notification.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import tn.smartbus.notification.dto.CreateNotificationRequest;
import tn.smartbus.notification.dto.NotificationResponse;
import tn.smartbus.notification.entity.Notification;
import tn.smartbus.notification.exception.ApiException;
import tn.smartbus.notification.mapper.NotificationMapper;
import tn.smartbus.notification.repository.NotificationRepository;

@Service
public class NotificationService {

    private final NotificationRepository repository;
    private final NotificationMapper mapper;

    public NotificationService(
            NotificationRepository repository,
            NotificationMapper mapper) {

        this.repository = repository;
        this.mapper = mapper;
    }

    public NotificationResponse create(
            CreateNotificationRequest request) {

        Notification existing = repository
                .findByEventIdAndRecipientId(
                        request.eventId(),
                        request.recipientId()
                )
                .orElse(null);

        if (existing != null) {
            return previousNotification(existing, request);
        }

        Notification notification = new Notification(
                UUID.randomUUID().toString(),
                null,
                request.eventId(),
                request.recipientId(),
                request.type(),
                request.tripId(),
                request.message(),
                Instant.now(),
                null
        );

        try {
            Notification saved = repository.insert(notification);
            return mapper.toResponse(saved);

        } catch (DuplicateKeyException exception) {

            // Another identical request may have arrived simultaneously.
            Notification saved = repository
                    .findByEventIdAndRecipientId(
                            request.eventId(),
                            request.recipientId()
                    )
                    .orElseThrow(() -> exception);

            return previousNotification(saved, request);
        }
    }

    public Page<NotificationResponse> getMine(
            String recipientId,
            int page,
            int size) {

        if (page < 0 || size < 1 || size > 100) {
            throw new ApiException(
                    400,
                    "Invalid page or size"
            );
        }

        return repository
                .findByRecipientIdOrderByCreatedAtDesc(
                        recipientId,
                        PageRequest.of(page, size)
                )
                .map(mapper::toResponse);
    }

    public long countUnread(String recipientId) {
        return repository.countByRecipientIdAndReadAtIsNull(
                recipientId
        );
    }

    public NotificationResponse markAsRead(
            String recipientId,
            String notificationId) {

        Notification notification = findOwned(
                recipientId,
                notificationId
        );

        if (notification.readAt() != null) {
            return mapper.toResponse(notification);
        }

        Notification updated = new Notification(
                notification.id(),
                notification.version(),
                notification.eventId(),
                notification.recipientId(),
                notification.type(),
                notification.tripId(),
                notification.message(),
                notification.createdAt(),
                Instant.now()
        );

        try {
            return mapper.toResponse(repository.save(updated));

        } catch (OptimisticLockingFailureException exception) {

            Notification current = findOwned(
                    recipientId,
                    notificationId
            );

            if (current.readAt() != null) {
                return mapper.toResponse(current);
            }

            throw ApiException.conflict(
                    "Notification changed. Refresh and try again"
            );
        }
    }

    private Notification findOwned(
            String recipientId,
            String notificationId) {

        return repository
                .findByIdAndRecipientId(
                        notificationId,
                        recipientId
                )
                .orElseThrow(() ->
                        ApiException.notFound("Notification not found")
                );
    }

    private NotificationResponse previousNotification(
            Notification existing,
            CreateNotificationRequest request) {

        if (existing.type() != request.type()
                || !existing.tripId().equals(request.tripId())
                || !existing.message().equals(request.message())) {

            throw ApiException.conflict(
                    "Event identifier already used with different information"
            );
        }

        return mapper.toResponse(existing);
    }
}