package tn.smartbus.transport.dto;

import jakarta.validation.constraints.*;

public record StationRequest(
    @NotBlank @Size(max = 100) String name,
    @DecimalMin("-90") @DecimalMax("90") Double latitude,
    @DecimalMin("-180") @DecimalMax("180") Double longitude) {}
