package tn.smartbus.trip.service;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;

import tn.smartbus.trip.dto.ReportBreakdownRequest;
import tn.smartbus.trip.dto.ReportDelayRequest;
import tn.smartbus.trip.dto.TripIncidentResponse;
import tn.smartbus.trip.entity.Trip;
import tn.smartbus.trip.entity.TripIncident;
import tn.smartbus.trip.entity.TripIncidentType;
import tn.smartbus.trip.entity.TripStatus;
import tn.smartbus.trip.exception.ApiException;
import tn.smartbus.trip.mapper.TripIncidentMapper;
import tn.smartbus.trip.repository.TripIncidentRepository;
import tn.smartbus.trip.repository.TripRepository;

@Service
public class TripIncidentService {

    private final TripRepository trips;
    private final TripIncidentRepository incidents;
    private final TripIncidentMapper mapper;
    private final TripNotificationEventService notificationEvents;

    public TripIncidentService(
            TripRepository trips,
            TripIncidentRepository incidents,
            TripIncidentMapper mapper,
            TripNotificationEventService notificationEvents) {

        this.trips = trips;
        this.incidents = incidents;
        this.mapper = mapper;
        this.notificationEvents = notificationEvents;
    }

    public TripIncidentResponse reportDelay(
            String tripId,
            String driverId,
            ReportDelayRequest request) {

        return record(
                tripId,
                driverId,
                request.requestId(),
                TripIncidentType.BUS_DELAYED,
                request.minutes()
        );
    }

    public TripIncidentResponse reportBreakdown(
            String tripId,
            String driverId,
            ReportBreakdownRequest request) {

        return record(
                tripId,
                driverId,
                request.requestId(),
                TripIncidentType.BUS_BREAKDOWN,
                null
        );
    }

    private TripIncidentResponse record(
            String tripId,
            String driverId,
            UUID requestId,
            TripIncidentType type,
            Integer delayMinutes) {

        Trip trip = trips.findById(tripId)
                .orElseThrow(() -> ApiException.missing("Trip"));

        if (!driverId.equals(trip.getDriverId())) {
            throw new ApiException(
                    403,
                    "This trip belongs to another driver"
            );
        }

        String incidentId = tripId + "-" + requestId;

        TripIncident existing = incidents.findById(incidentId)
                .orElse(null);

        if (existing != null) {
            return previousIncident(existing, type, delayMinutes);
        }

        if (trip.getStatus() != TripStatus.PLANNED
                && trip.getStatus() != TripStatus.IN_PROGRESS) {

            throw ApiException.conflict(
                    "Incidents cannot be reported for this trip"
            );
        }

        TripIncident incident = new TripIncident(
                incidentId,
                tripId,
                driverId,
                type,
                trip.getRouteName(),
                trip.getRegistrationNumber(),
                delayMinutes,
                Instant.now()
        );

        try {
            TripIncident saved = incidents.insert(incident);

            notificationEvents.incidentReported(saved);

            return mapper.toResponse(saved);

        } catch (DuplicateKeyException exception) {

            TripIncident saved = incidents.findById(incidentId)
                    .orElseThrow(() -> exception);

            return previousIncident(saved, type, delayMinutes);
        }
    }

    private TripIncidentResponse previousIncident(
            TripIncident incident,
            TripIncidentType requestedType,
            Integer requestedMinutes) {

        if (incident.type() != requestedType
                || !Objects.equals(
                        incident.delayMinutes(),
                        requestedMinutes
                )) {

            throw ApiException.conflict(
                    "Request identifier already used with different information"
            );
        }

        notificationEvents.incidentReported(incident);

        return mapper.toResponse(incident);
    }
}