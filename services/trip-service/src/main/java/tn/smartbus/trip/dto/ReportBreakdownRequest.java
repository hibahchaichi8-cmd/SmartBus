package tn.smartbus.trip.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record ReportBreakdownRequest(

        @NotNull(message = "Request identifier is required")
        UUID requestId

) {
}