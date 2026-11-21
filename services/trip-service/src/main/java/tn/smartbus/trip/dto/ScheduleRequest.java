package tn.smartbus.trip.dto;

import jakarta.validation.constraints.*;

public record ScheduleRequest(
    @NotBlank @Pattern(regexp = "[0-2][0-9]:[0-5][0-9]") String firstDeparture,
    @NotBlank @Pattern(regexp = "[0-2][0-9]:[0-5][0-9]") String lastDeparture,
    @Min(1) @Max(240) int intervalMinutes) {}
