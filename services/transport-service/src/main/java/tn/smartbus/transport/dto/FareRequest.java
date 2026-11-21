package tn.smartbus.transport.dto;

import jakarta.validation.constraints.*;

public record FareRequest(@NotBlank String routeId, @Min(0) @Max(100000) int amountMillimes) {}
