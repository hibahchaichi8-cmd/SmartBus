package tn.smartbus.notification.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import tn.smartbus.notification.entity.Notification;

public interface NotificationRepository
        extends MongoRepository<Notification, String> {

    Page<Notification> findByRecipientIdOrderByCreatedAtDesc(
            String recipientId,
            Pageable pageable
    );

    long countByRecipientIdAndReadAtIsNull(
            String recipientId
    );

    Optional<Notification> findByIdAndRecipientId(
            String id,
            String recipientId
    );

    Optional<Notification> findByEventIdAndRecipientId(
            String eventId,
            String recipientId
    );
}