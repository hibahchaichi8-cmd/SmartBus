package tn.smartbus.ticket.controller;

import java.net.URI;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tn.smartbus.ticket.dto.PurchaseRequest;
import tn.smartbus.ticket.dto.QrResponse;
import tn.smartbus.ticket.dto.TicketPageResponse;
import tn.smartbus.ticket.dto.TicketResponse;
import tn.smartbus.ticket.dto.ValidateTicketRequest;
import tn.smartbus.ticket.service.TicketService;

@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private final TicketService service;

    public TicketController(TicketService service) {
        this.service = service;
    }

    @PostMapping("/purchase")
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<TicketResponse> purchase(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody PurchaseRequest request) {

        TicketResponse ticket = service.purchase(
                jwt.getSubject(),
                request
        );

        return ResponseEntity
                .created(URI.create("/api/v1/tickets/" + ticket.id()))
                .cacheControl(CacheControl.noStore())
                .body(ticket);
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<TicketPageResponse> getMyTickets(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<TicketResponse> tickets = service.getMyTickets(
                jwt.getSubject(),
                page,
                size
        );

        TicketPageResponse response = new TicketPageResponse(
                tickets.getContent(),
                tickets.getNumber(),
                tickets.getSize(),
                tickets.getTotalElements(),
                tickets.getTotalPages()
        );

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<TicketResponse> getMyTicket(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String id) {

        TicketResponse ticket = service.getMyTicket(
                jwt.getSubject(),
                id
        );

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(ticket);
    }

    @GetMapping("/{id}/qr")
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<QrResponse> getQr(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable String id) {

        String token = service.getQrToken(
                jwt.getSubject(),
                id
        );

        QrResponse response = new QrResponse(id, token);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(response);
    }

    @PostMapping("/validate")
    @PreAuthorize("hasRole('DRIVER')")
    public ResponseEntity<TicketResponse> validate(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ValidateTicketRequest request) {

        TicketResponse ticket = service.validate(
                jwt.getSubject(),
                request
        );

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(ticket);
    }
}