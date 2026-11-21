package tn.smartbus.trip.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import tn.smartbus.trip.entity.Trip;
import tn.smartbus.trip.entity.TripStatus;

public interface TripRepository extends MongoRepository<Trip, String> {

    List<Trip> findByDriverIdOrderByPlannedDepartureDesc(
            String driverId
    );

    List<Trip> findByStatus(
            TripStatus status
    );

    boolean existsByRouteIdAndStatusIn(
            String routeId,
            List<TripStatus> statuses
    );

    boolean existsByBusIdAndStatusIn(
            String busId,
            List<TripStatus> statuses
    );

    List<Trip> findByStatusAndPlannedDepartureAfter(
            TripStatus status,
            Instant departure,
            Pageable pageable
    );

    List<Trip> findByRouteIdAndStatusAndPlannedDepartureAfter(
            String routeId,
            TripStatus status,
            Instant departure,
            Pageable pageable
    );
    boolean existsByDriverId(String driverId);
}