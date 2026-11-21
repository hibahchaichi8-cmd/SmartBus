package tn.smartbus.trip.dto;

import java.util.UUID;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ReportDelayRequest(

        @NotNull(message = "Request identifier is required")
        UUID requestId,

        @Min(value = 1, message = "Delay must be at least one minute")
        @Max(value = 1440, message = "Delay cannot exceed 1440 minutes")
        int minutes

) {
}