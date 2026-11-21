package tn.smartbus.ticket.dto;

import java.util.List;

public record TicketPageResponse(
        List<TicketResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}