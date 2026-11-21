package tn.smartbus.trip.dto;

import java.time.Instant;

public record AvailableTripResponse(
        String id,
        String routeId,
        String routeName,
        String registrationNumber,
        Instant plannedDeparture,
        Instant estimatedArrival,
        int amountMillimes,
        String currency
) {
}