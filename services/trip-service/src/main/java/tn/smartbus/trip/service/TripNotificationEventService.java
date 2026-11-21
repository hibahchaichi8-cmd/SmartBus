package tn.smartbus.trip.service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import tn.smartbus.trip.dto.PendingNotificationEvent;
import tn.smartbus.trip.entity.Trip;
import tn.smartbus.trip.entity.TripIncident;
import tn.smartbus.trip.entity.TripIncidentType;
import tn.smartbus.trip.entity.TripNotificationEvent;
import tn.smartbus.trip.entity.TripNotificationType;
import tn.smartbus.trip.mapper.TripNotificationEventMapper;
import tn.smartbus.trip.repository.TripNotificationEventRepository;

@Service
public class TripNotificationEventService {

    private final TripNotificationEventRepository repository;
    private final TripNotificationEventMapper mapper;

    public TripNotificationEventService(
            TripNotificationEventRepository repository,
            TripNotificationEventMapper mapper) {

        this.repository = repository;
        this.mapper = mapper;
    }

    public void tripAssigned(Trip trip) {

        String departure = DateTimeFormatter
                .ofPattern("dd/MM/yyyy HH:mm")
                .withZone(ZoneId.of("Africa/Tunis"))
                .format(trip.getPlannedDeparture());

        String message = "New trip assigned — "
                + trip.getRouteName()
                + ", bus "
                + trip.getRegistrationNumber()
                + ", departure "
                + departure
                + ".";

        saveEvent(new TripNotificationEvent(
                trip.getId() + "-assigned",
                trip.getDriverId(),
                TripNotificationType.TRIP_ASSIGNED,
                trip.getId(),
                message,
                Instant.now()
        ));
    }

    public void tripStarted(Trip trip) {

        String message = "Your bus has started — "
                + trip.getRegistrationNumber()
                + ", "
                + trip.getRouteName()
                + ".";

        saveEvent(new TripNotificationEvent(
                trip.getId() + "-started",
                null,
                TripNotificationType.TRIP_STARTED,
                trip.getId(),
                message,
                trip.getActualStart()
        ));
    }

    public void tripCompleted(Trip trip) {

        String message = "Trip completed — "
                + trip.getRouteName()
                + ", bus "
                + trip.getRegistrationNumber()
                + ".";

        saveEvent(new TripNotificationEvent(
                trip.getId() + "-completed",
                trip.getDriverId(),
                TripNotificationType.TRIP_COMPLETED,
                trip.getId(),
                message,
                trip.getActualEnd()
        ));
    }

    public void incidentReported(TripIncident incident) {

        TripNotificationType type;
        String message;

        if (incident.type() == TripIncidentType.BUS_DELAYED) {

            type = TripNotificationType.BUS_DELAYED;

            message = "Delay reported — bus "
                    + incident.registrationNumber()
                    + ", "
                    + incident.routeName()
                    + ", estimated delay: "
                    + incident.delayMinutes()
                    + " minutes.";

        } else {

            type = TripNotificationType.BUS_BREAKDOWN;

            message = "Breakdown reported — bus "
                    + incident.registrationNumber()
                    + ", "
                    + incident.routeName()
                    + " is currently immobilized.";
        }

        saveEvent(new TripNotificationEvent(
                "incident-" + incident.id(),
                null,
                type,
                incident.tripId(),
                message,
                incident.reportedAt()
        ));
    }

    public List<PendingNotificationEvent> pending() {

        PageRequest page = PageRequest.of(
                0,
                50,
                Sort.by("occurredAt").ascending()
        );

        return repository.findAll(page)
                .map(mapper::toResponse)
                .getContent();
    }

    public void acknowledge(String eventId) {
        repository.deleteById(eventId);
    }

    private void saveEvent(TripNotificationEvent event) {

        try {
            repository.insert(event);

        } catch (DuplicateKeyException exception) {

            // The same event may already be waiting for delivery.
            if (!repository.existsById(event.id())) {
                throw exception;
            }
        }
    }
}