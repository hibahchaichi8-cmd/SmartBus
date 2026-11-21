package tn.smartbus.trip.dto;

import java.time.Instant;

public record JourneyResponse(
    String routeId,
    String routeName,
    String origin,
    String destination,
    Instant departure,
    Instant estimatedArrival,
    int durationMinutes,
    int amountMillimes,
    String currency,
    String source) {}
