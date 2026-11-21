package tn.smartbus.trip.dto;

import java.time.Instant;
import java.util.List;
import tn.smartbus.trip.entity.*;

public record TripResponse(
    String id,
    String routeId,
    String routeName,
    String busId,
    String registrationNumber,
    int capacity,
    String driverId,
    String driverName,
    Instant plannedDeparture,
    Instant actualStart,
    Instant actualEnd,
    TripStatus status,
    List<TripStop> stops,
    List<Checkpoint> checkpoints,
    Instant estimatedArrival,
    boolean overdue) {}
