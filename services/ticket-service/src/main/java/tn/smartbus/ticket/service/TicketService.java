package tn.smartbus.ticket.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.ArrayList;
import tn.smartbus.ticket.dto.FareDetails;
import tn.smartbus.ticket.dto.PurchaseRequest;
import tn.smartbus.ticket.dto.TicketResponse;
import tn.smartbus.ticket.dto.TripDetails;
import tn.smartbus.ticket.dto.ValidateTicketRequest;
import tn.smartbus.ticket.entity.Ticket;
import tn.smartbus.ticket.entity.TicketStatus;
import tn.smartbus.ticket.exception.ApiException;
import tn.smartbus.ticket.mapper.TicketMapper;
import tn.smartbus.ticket.repository.TicketRepository;

@Service
public class TicketService {

    private final TicketRepository repository;
    private final TicketMapper mapper;
    private final TripCatalogClient catalog;
    private final QrTokenService qrTokens;

    public TicketService(
            TicketRepository repository,
            TicketMapper mapper,
            TripCatalogClient catalog,
            QrTokenService qrTokens) {

        this.repository = repository;
        this.mapper = mapper;
        this.catalog = catalog;
        this.qrTokens = qrTokens;
    }

    public TicketResponse purchase(
            String citizenId,
            PurchaseRequest request) {

        String requestId = request.purchaseRequestId().toString();

        // Return the previous result when the same purchase is retried.
        Ticket existing = repository
                .findByCitizenIdAndPurchaseRequestId(citizenId, requestId)
                .orElse(null);

        if (existing != null) {
            return previousPurchase(existing, request.tripId());
        }

        TripDetails trip = catalog.getTrip(request.tripId());

        if (!"PLANNED".equals(trip.status())) {
            throw ApiException.conflict(
                    "Tickets are available only for planned trips"
            );
        }

        Instant now = Instant.now();

        if (trip.plannedDeparture() == null
                || !trip.plannedDeparture().isAfter(now)) {
            throw ApiException.conflict(
                    "Ticket sales are closed for this trip"
            );
        }

        FareDetails fare = catalog.getFare(trip.routeId());

        if (fare.amountMillimes() < 0
                || !trip.routeId().equals(fare.routeId())) {
            throw new ApiException(
                    503,
                    "Invalid fare information received"
            );
        }

        String ticketId = UUID.randomUUID().toString();
        String token = qrTokens.generateToken(ticketId);
        String tokenHash = qrTokens.hashToken(token);

        // Simulated payment: confirming purchase accepts the simulation.
        // No bank or real payment provider is contacted.
        Ticket ticket = new Ticket(
                ticketId,
                null,
                citizenId,
                trip.id(),
                trip.routeName(),
                trip.registrationNumber(),
                trip.plannedDeparture(),
                fare.amountMillimes(),
                "TND",
                TicketStatus.VALID,
                now,
                null,
                null,
                tokenHash,
                requestId
        );

        try {
            Ticket saved = repository.save(ticket);
            return mapper.toResponse(saved);

        } catch (DuplicateKeyException exception) {

            // Two identical requests may arrive at the same time.
            Ticket saved = repository
                    .findByCitizenIdAndPurchaseRequestId(
                            citizenId,
                            requestId
                    )
                    .orElseThrow(() -> exception);

            return previousPurchase(saved, request.tripId());
        }
    }

    public Page<TicketResponse> getMyTickets(
            String citizenId,
            int page,
            int size) {

        if (page < 0 || size < 1 || size > 100) {
            throw new ApiException(
                    400,
                    "Invalid page or size"
            );
        }

        return repository
                .findByCitizenIdOrderByPurchasedAtDesc(
                        citizenId,
                        PageRequest.of(page, size)
                )
                .map(mapper::toResponse);
    }

    public TicketResponse getMyTicket(
            String citizenId,
            String ticketId) {

        Ticket ticket = findOwnedTicket(citizenId, ticketId);
        return mapper.toResponse(ticket);
    }

    public String getQrToken(
            String citizenId,
            String ticketId) {

        Ticket ticket = findOwnedTicket(citizenId, ticketId);

        if (ticket.status() != TicketStatus.VALID) {
            throw ApiException.conflict(
                    "This ticket is no longer available for validation"
            );
        }

        String token = qrTokens.generateToken(ticket.id());

        if (!qrTokens.hashToken(token).equals(ticket.qrTokenHash())) {
            throw new ApiException(
                    503,
                    "Ticket QR configuration is unavailable"
            );
        }

        return token;
    }

    public TicketResponse validate(
            String driverId,
            ValidateTicketRequest request) {

        String tokenHash = qrTokens.hashToken(request.qrToken());

        Ticket ticket = repository
                .findByQrTokenHash(tokenHash)
                .orElseThrow(() ->
                        ApiException.notFound("Invalid ticket")
                );

        TripDetails trip = catalog.getTrip(ticket.tripId());

        if (!driverId.equals(trip.driverId())) {
            throw ApiException.forbidden(
                    "This ticket belongs to another driver's trip"
            );
        }

        if (!"IN_PROGRESS".equals(trip.status())) {
            throw ApiException.conflict(
                    "The ticket's trip is not in progress"
            );
        }

        if (ticket.status() != TicketStatus.VALID) {
            throw ApiException.conflict(
                    "Ticket already used or cancelled"
            );
        }

        Ticket usedTicket = new Ticket(
                ticket.id(),
                ticket.version(),
                ticket.citizenId(),
                ticket.tripId(),
                ticket.routeName(),
                ticket.registrationNumber(),
                ticket.plannedDeparture(),
                ticket.amountMillimes(),
                ticket.currency(),
                TicketStatus.USED,
                ticket.purchasedAt(),
                Instant.now(),
                driverId,
                ticket.qrTokenHash(),
                ticket.purchaseRequestId()
        );

        // The version prevents two simultaneous validations from succeeding.
        Ticket saved = repository.save(usedTicket);

        return mapper.toResponse(saved);
    }

    private Ticket findOwnedTicket(
            String citizenId,
            String ticketId) {

        return repository
                .findByIdAndCitizenId(ticketId, citizenId)
                .orElseThrow(() ->
                        ApiException.notFound("Ticket not found")
                );
    }

    private TicketResponse previousPurchase(
            Ticket ticket,
            String requestedTripId) {

        if (!ticket.tripId().equals(requestedTripId)) {
            throw ApiException.conflict(
                    "Purchase request ID already used for another trip"
            );
        }

        return mapper.toResponse(ticket);
    }
    public boolean hasTickets(String tripId) {

    if (tripId == null || tripId.isBlank()) {
        throw new ApiException(
                400,
                "Trip identifier is required"
        );
    }

    return repository.existsByTripId(tripId);
}
public List<String> getNotificationRecipients(
        String tripId,
        Instant eventTime) {

    if (tripId == null || tripId.isBlank() || eventTime == null) {
        throw new ApiException(
                400,
                "Trip and event time are required"
        );
    }

    List<String> recipients = new ArrayList<>();

    for (Ticket ticket : repository.findByTripId(tripId)) {

        if (ticket.status() == TicketStatus.CANCELLED) {
            continue;
        }

        // A ticket purchased after the event is not concerned.
        if (ticket.purchasedAt().isAfter(eventTime)) {
            continue;
        }

        // A ticket already used before the event is not concerned.
        if (ticket.validatedAt() != null
                && !ticket.validatedAt().isAfter(eventTime)) {
            continue;
        }

        if (!recipients.contains(ticket.citizenId())) {
            recipients.add(ticket.citizenId());
        }
    }

    return recipients;
}
}