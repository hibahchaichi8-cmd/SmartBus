package tn.smartbus.ticket.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record PurchaseRequest(

        @NotBlank(message = "Trip is required")
        @Pattern(
                regexp = "[A-Za-z0-9-]{1,80}",
                message = "Invalid trip identifier"
        )
        String tripId,

        @NotNull(message = "Purchase request ID is required")
        UUID purchaseRequestId

) {
}