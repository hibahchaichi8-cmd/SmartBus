package tn.smartbus.trip.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import tn.smartbus.trip.entity.TripNotificationEvent;

public interface TripNotificationEventRepository
        extends MongoRepository<TripNotificationEvent, String> {
}