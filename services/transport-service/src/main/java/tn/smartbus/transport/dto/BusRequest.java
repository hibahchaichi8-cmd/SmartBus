package tn.smartbus.transport.dto;

import jakarta.validation.constraints.*;

public record BusRequest(
    @NotBlank @Size(max = 30) String registrationNumber, @Min(1) @Max(200) int capacity) {}
