package tn.smartbus.trip.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tn.smartbus.trip.dto.CheckpointRequest;
import tn.smartbus.trip.dto.TripRequest;
import tn.smartbus.trip.dto.TripResponse;
import tn.smartbus.trip.service.TripService;

@RestController
@RequestMapping("/api/v1/trips")
public class TripController {

    private final TripService service;

    public TripController(TripService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<TripResponse> list() {
        return service.list();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TripResponse> create(
            @Valid @RequestBody TripRequest request) {

        TripResponse trip = service.create(request);

        return ResponseEntity.status(201).body(trip);
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('DRIVER')")
    public List<TripResponse> mine(
            @AuthenticationPrincipal Jwt jwt) {

        return service.mine(jwt.getSubject());
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasRole('DRIVER')")
    public TripResponse start(
            @PathVariable String id,
            @AuthenticationPrincipal Jwt jwt) {

        return service.start(id, jwt.getSubject());
    }

    @PostMapping("/{id}/checkpoints")
    @PreAuthorize("hasRole('DRIVER')")
    public TripResponse checkpoint(
            @PathVariable String id,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CheckpointRequest request) {

        return service.checkpoint(
                id,
                jwt.getSubject(),
                request.stopIndex()
        );
    }

    @PostMapping("/{id}/finish")
    @PreAuthorize("hasRole('DRIVER')")
    public TripResponse finish(
            @PathVariable String id,
            @AuthenticationPrincipal Jwt jwt) {

        return service.finish(id, jwt.getSubject());
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADMIN')")
    public TripResponse cancel(
            @PathVariable String id,
            @AuthenticationPrincipal Jwt jwt) {

        return service.cancel(id, jwt.getTokenValue());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(
            @PathVariable String id,
            @AuthenticationPrincipal Jwt jwt) {

        service.delete(id, jwt.getTokenValue());

        return ResponseEntity.noContent().build();
    }
}