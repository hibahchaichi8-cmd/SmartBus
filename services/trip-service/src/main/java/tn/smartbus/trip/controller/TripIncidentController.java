package tn.smartbus.trip.controller;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tn.smartbus.trip.dto.ReportBreakdownRequest;
import tn.smartbus.trip.dto.ReportDelayRequest;
import tn.smartbus.trip.dto.TripIncidentResponse;
import tn.smartbus.trip.service.TripIncidentService;

@RestController
@RequestMapping("/api/v1/trips/{tripId}/incidents")
@PreAuthorize("hasRole('DRIVER')")
public class TripIncidentController {

    private final TripIncidentService service;

    public TripIncidentController(TripIncidentService service) {
        this.service = service;
    }

    @PostMapping("/delay")
    public ResponseEntity<TripIncidentResponse> reportDelay(
            @PathVariable String tripId,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ReportDelayRequest request) {

        TripIncidentResponse response = service.reportDelay(
                tripId,
                jwt.getSubject(),
                request
        );

        return ResponseEntity.status(201).body(response);
    }

    @PostMapping("/breakdown")
    public ResponseEntity<TripIncidentResponse> reportBreakdown(
            @PathVariable String tripId,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ReportBreakdownRequest request) {

        TripIncidentResponse response = service.reportBreakdown(
                tripId,
                jwt.getSubject(),
                request
        );

        return ResponseEntity.status(201).body(response);
    }
}