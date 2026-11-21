package tn.smartbus.ticket.mapper;

import org.springframework.stereotype.Component;

import tn.smartbus.ticket.dto.TicketResponse;
import tn.smartbus.ticket.entity.Ticket;

@Component
public class TicketMapper {

    public TicketResponse toResponse(Ticket ticket) {
        return new TicketResponse(
                ticket.id(),
                ticket.tripId(),
                ticket.routeName(),
                ticket.registrationNumber(),
                ticket.plannedDeparture(),
                ticket.amountMillimes(),
                ticket.currency(),
                ticket.status(),
                ticket.purchasedAt(),
                ticket.validatedAt()
        );
    }
}
