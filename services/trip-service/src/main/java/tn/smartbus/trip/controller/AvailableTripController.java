package tn.smartbus.trip.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tn.smartbus.trip.dto.AvailableTripResponse;
import tn.smartbus.trip.service.AvailableTripService;

@RestController
@RequestMapping("/api/v1/trips/available")
public class AvailableTripController {

    private final AvailableTripService service;

    public AvailableTripController(AvailableTripService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("hasRole('CITIZEN')")
    public List<AvailableTripResponse> list(
            @RequestParam(required = false) String routeId) {

        return service.list(routeId);
    }
}