package tn.smartbus.trip.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import tn.smartbus.trip.dto.AvailableTripResponse;
import tn.smartbus.trip.dto.CatalogDtos.Fare;
import tn.smartbus.trip.entity.Trip;
import tn.smartbus.trip.entity.TripStatus;
import tn.smartbus.trip.exception.ApiException;
import tn.smartbus.trip.mapper.AvailableTripMapper;
import tn.smartbus.trip.repository.TripRepository;

@Service
public class AvailableTripService {

    private final TripRepository repository;
    private final CatalogClient catalog;
    private final AvailableTripMapper mapper;

    public AvailableTripService(
            TripRepository repository,
            CatalogClient catalog,
            AvailableTripMapper mapper) {

        this.repository = repository;
        this.catalog = catalog;
        this.mapper = mapper;
    }

    public List<AvailableTripResponse> list(String routeId) {

        PageRequest page = PageRequest.of(
                0,
                100,
                Sort.by("plannedDeparture").ascending()
        );

        Instant now = Instant.now();
        List<Trip> trips;

        if (routeId == null || routeId.isBlank()) {

            trips = repository.findByStatusAndPlannedDepartureAfter(
                    TripStatus.PLANNED,
                    now,
                    page
            );

        } else {

            trips = repository
                    .findByRouteIdAndStatusAndPlannedDepartureAfter(
                            routeId,
                            TripStatus.PLANNED,
                            now,
                            page
                    );
        }

        List<AvailableTripResponse> result = new ArrayList<>();

        for (Trip trip : trips) {
            try {
                Fare fare = catalog.fare(trip.getRouteId());

                result.add(mapper.toResponse(trip, fare));

            } catch (ApiException exception) {

                // A route without a fare is not available for purchase.
                if (exception.status != 404) {
                    throw exception;
                }
            }
        }

        return result;
    }
}