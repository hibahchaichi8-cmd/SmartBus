package tn.smartbus.trip.dto;

import jakarta.validation.constraints.*;
import java.time.OffsetDateTime;

public record TripRequest(
    @NotBlank String routeId,
    @NotBlank String busId,
    @NotBlank String driverId,
    @NotNull @FutureOrPresent OffsetDateTime plannedDeparture) {}
