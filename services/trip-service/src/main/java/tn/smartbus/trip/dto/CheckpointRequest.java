package tn.smartbus.trip.dto;

import jakarta.validation.constraints.Min;

public record CheckpointRequest(@Min(0) int stopIndex) {}
