package tn.smartbus.ticket.controller;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tn.smartbus.ticket.exception.ApiException;
import tn.smartbus.ticket.service.TicketService;

@RestController
@RequestMapping("/internal/tickets")
public class InternalTicketController {

    private final TicketService service;

    public InternalTicketController(TicketService service) {
        this.service = service;
    }

    @GetMapping("/recipients")
    public List<String> recipients(
            @RequestParam(defaultValue = "") String tripId,
            @RequestParam(defaultValue = "") String at) {

        Instant eventTime;

        try {
            eventTime = Instant.parse(at);
        } catch (DateTimeParseException exception) {
            throw new ApiException(
                    400,
                    "Invalid event timestamp"
            );
        }

        return service.getNotificationRecipients(
                tripId,
                eventTime
        );
    }
}