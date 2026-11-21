package tn.smartbus.transport.entity;

import jakarta.validation.constraints.*;

public record RouteStop(@NotBlank String stationId, @Min(0) @Max(1440) int offsetMinutes) {}
