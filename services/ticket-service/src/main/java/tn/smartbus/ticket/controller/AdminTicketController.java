package tn.smartbus.ticket.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tn.smartbus.ticket.service.TicketService;

@RestController
@RequestMapping("/api/v1/admin/tickets")
@PreAuthorize("hasRole('ADMIN')")
public class AdminTicketController {

    private final TicketService service;

    public AdminTicketController(TicketService service) {
        this.service = service;
    }

    @GetMapping("/exists")
    public boolean exists(
            @RequestParam(defaultValue = "") String tripId) {

        return service.hasTickets(tripId);
    }
}