package tn.smartbus.ticket.dto;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record TripDetails(
        String id,
        String routeId,
        String routeName,
        String registrationNumber,
        String driverId,
        Instant plannedDeparture,
        String status
) {
}