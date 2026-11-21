package tn.smartbus.ticket.dto;

import java.time.Instant;

import tn.smartbus.ticket.entity.TicketStatus;

public record TicketResponse(

        String id,

        String tripId,

        String routeName,

        String registrationNumber,

        Instant plannedDeparture,

        int amountMillimes,

        String currency,

        TicketStatus status,

        Instant purchasedAt,

        Instant validatedAt

) {
}