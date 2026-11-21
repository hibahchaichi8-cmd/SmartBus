package tn.smartbus.trip.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import tn.smartbus.trip.entity.TripIncident;

public interface TripIncidentRepository
        extends MongoRepository<TripIncident, String> {
}