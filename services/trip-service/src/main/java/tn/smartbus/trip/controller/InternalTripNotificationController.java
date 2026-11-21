package tn.smartbus.trip.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tn.smartbus.trip.dto.PendingNotificationEvent;
import tn.smartbus.trip.service.TripNotificationEventService;

@RestController
@RequestMapping("/internal/notification-events")
public class InternalTripNotificationController {

    private final TripNotificationEventService service;

    public InternalTripNotificationController(
            TripNotificationEventService service) {

        this.service = service;
    }

    @GetMapping
    public List<PendingNotificationEvent> pending() {
        return service.pending();
    }

    @PostMapping("/{eventId}/ack")
    public ResponseEntity<Void> acknowledge(
            @PathVariable String eventId) {

        service.acknowledge(eventId);

        return ResponseEntity.noContent().build();
    }
}