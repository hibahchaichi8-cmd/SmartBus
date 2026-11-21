package tn.smartbus.trip.dto;

import java.time.Instant;

import tn.smartbus.trip.entity.TripIncidentType;

public record TripIncidentResponse(
        String id,
        TripIncidentType type,
        Integer delayMinutes,
        Instant reportedAt
) {
}