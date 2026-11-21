package tn.smartbus.ticket.dto;

public record QrResponse(
        String ticketId,
        String qrToken
) {
}