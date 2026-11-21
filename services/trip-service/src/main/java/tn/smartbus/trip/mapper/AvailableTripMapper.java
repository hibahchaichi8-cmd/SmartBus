package tn.smartbus.trip.mapper;

import java.time.Instant;

import org.springframework.stereotype.Component;

import tn.smartbus.trip.dto.AvailableTripResponse;
import tn.smartbus.trip.dto.CatalogDtos.Fare;
import tn.smartbus.trip.entity.Trip;

@Component
public class AvailableTripMapper {

    public AvailableTripResponse toResponse(Trip trip, Fare fare) {

        int lastStopIndex = trip.getStops().size() - 1;

        int durationMinutes = trip.getStops()
                .get(lastStopIndex)
                .offsetMinutes();

        Instant estimatedArrival = trip.getPlannedDeparture()
                .plusSeconds(durationMinutes * 60L);

        return new AvailableTripResponse(
                trip.getId(),
                trip.getRouteId(),
                trip.getRouteName(),
                trip.getRegistrationNumber(),
                trip.getPlannedDeparture(),
                estimatedArrival,
                fare.amountMillimes(),
                "TND"
        );
    }
}