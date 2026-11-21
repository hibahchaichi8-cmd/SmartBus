package tn.smartbus.ticket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ValidateTicketRequest(

        @NotBlank(message = "QR token is required")
        @Size(
                min = 32,
                max = 256,
                message = "Invalid QR token length"
        )
        String qrToken

) {
}