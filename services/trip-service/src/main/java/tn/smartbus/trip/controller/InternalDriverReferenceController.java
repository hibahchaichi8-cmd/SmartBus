package tn.smartbus.trip.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tn.smartbus.trip.repository.TripRepository;

@RestController
@RequestMapping("/internal/drivers")
public class InternalDriverReferenceController {

    private final TripRepository repository;

    public InternalDriverReferenceController(
            TripRepository repository) {

        this.repository = repository;
    }

    @GetMapping("/{id}/has-trips")
    public boolean hasTrips(@PathVariable String id) {
        return repository.existsByDriverId(id);
    }
}