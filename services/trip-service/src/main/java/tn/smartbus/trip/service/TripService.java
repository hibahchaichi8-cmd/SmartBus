package tn.smartbus.trip.service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import tn.smartbus.trip.dto.TripRequest;
import tn.smartbus.trip.dto.TripResponse;
import tn.smartbus.trip.entity.Checkpoint;
import tn.smartbus.trip.entity.Trip;
import tn.smartbus.trip.entity.TripStatus;
import tn.smartbus.trip.entity.TripStop;
import tn.smartbus.trip.exception.ApiException;
import tn.smartbus.trip.mapper.TripMapper;
import tn.smartbus.trip.repository.TripRepository;

@Service
public class TripService {

    private final TripRepository repository;
    private final TripMapper mapper;
    private final CatalogClient catalog;
    private final TicketClient tickets;
    private final TripNotificationEventService notificationEvents;

    public TripService(
            TripRepository repository,
            TripMapper mapper,
            CatalogClient catalog,
            TicketClient tickets,
            TripNotificationEventService notificationEvents) {

        this.repository = repository;
        this.mapper = mapper;
        this.catalog = catalog;
        this.tickets = tickets;
        this.notificationEvents = notificationEvents;
    }

    public Trip entity(String id) {
        return repository.findById(id)
                .orElseThrow(() -> ApiException.missing("Trip"));
    }

    public List<TripResponse> list() {
        return repository.findAll(
                        PageRequest.of(
                                0,
                                200,
                                Sort.by(
                                        Sort.Direction.DESC,
                                        "plannedDeparture"
                                )
                        )
                )
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    public List<TripResponse> mine(String driverId) {
        return repository
                .findByDriverIdOrderByPlannedDepartureDesc(driverId)
                .stream()
                .limit(200)
                .map(mapper::toResponse)
                .toList();
    }

    public synchronized TripResponse create(TripRequest request) {

        var route = catalog.route(request.routeId());
        var bus = catalog.bus(request.busId());
        var driver = catalog.driver(request.driverId());

        int lastStopIndex = route.stops().size() - 1;

        int duration = route.stops()
                .get(lastStopIndex)
                .offsetMinutes();

        Instant start = request.plannedDeparture().toInstant();
        Instant end = start.plusSeconds(duration * 60L);

        if (start.isBefore(Instant.now().minusSeconds(5))) {
            throw new ApiException(
                    400,
                    "Planned departure must be in the future"
            );
        }

        for (Trip existing : repository.findAll()) {

            if (existing.getStatus() == TripStatus.CANCELLED
                    || existing.getStatus() == TripStatus.COMPLETED) {
                continue;
            }

            int existingLastStop = existing.getStops().size() - 1;

            int existingDuration = existing.getStops()
                    .get(existingLastStop)
                    .offsetMinutes();

            Instant existingEnd = existing.getPlannedDeparture()
                    .plusSeconds(existingDuration * 60L);

            boolean timesOverlap =
                    start.isBefore(existingEnd)
                    && end.isAfter(existing.getPlannedDeparture());

            boolean sameBus =
                    existing.getBusId().equals(bus.id());

            boolean sameDriver =
                    existing.getDriverId().equals(driver.id());

            if (timesOverlap && (sameBus || sameDriver)) {
                throw ApiException.conflict(
                        "Bus or driver already has an overlapping trip"
                );
            }
        }

        Trip trip = new Trip();

        trip.setId(UUID.randomUUID().toString());
        trip.setRouteId(route.id());
        trip.setRouteName(route.code() + " · " + route.name());
        trip.setBusId(bus.id());
        trip.setRegistrationNumber(bus.registrationNumber());
        trip.setCapacity(bus.capacity());
        trip.setDriverId(driver.id());
        trip.setDriverName(driver.fullName());
        trip.setPlannedDeparture(start);
        trip.setStatus(TripStatus.PLANNED);
        trip.setCheckpoints(new ArrayList<>());

        List<TripStop> stops = new ArrayList<>();

        for (var stop : route.stops()) {
            var station = catalog.station(stop.stationId());

            stops.add(new TripStop(
                    station.id(),
                    station.name(),
                    stop.offsetMinutes(),
                    station.latitude(),
                    station.longitude()
            ));
        }

        trip.setStops(stops);

        Trip saved = repository.save(trip);

        notificationEvents.tripAssigned(saved);

        return mapper.toResponse(saved);
    }

    private Trip owned(String id, String driverId) {

        Trip trip = entity(id);

        if (!trip.getDriverId().equals(driverId)) {
            throw new ApiException(
                    403,
                    "This trip belongs to another driver"
            );
        }

        return trip;
    }

    public synchronized TripResponse start(
            String id,
            String driverId) {

        Trip trip = owned(id, driverId);

        if (trip.getStatus() != TripStatus.PLANNED) {
            throw ApiException.conflict(
                    "Only planned trips can start"
            );
        }

        for (Trip activeTrip :
                repository.findByStatus(TripStatus.IN_PROGRESS)) {

            boolean sameBus =
                    activeTrip.getBusId().equals(trip.getBusId());

            boolean sameDriver =
                    activeTrip.getDriverId().equals(driverId);

            if (sameBus || sameDriver) {
                throw ApiException.conflict(
                        "Bus or driver already has an active trip"
                );
            }
        }

        trip.setActualStart(Instant.now());
        trip.setStatus(TripStatus.IN_PROGRESS);

        Trip saved = repository.save(trip);

        notificationEvents.tripStarted(saved);

        return mapper.toResponse(saved);
    }

    public TripResponse checkpoint(
            String id,
            String driverId,
            int index) {

        Trip trip = owned(id, driverId);

        if (trip.getStatus() != TripStatus.IN_PROGRESS) {
            throw ApiException.conflict(
                    "Start this trip first"
            );
        }

        if (index != trip.getCheckpoints().size()
                || index >= trip.getStops().size()) {
            throw ApiException.conflict(
                    "Confirm the next stop in route order"
            );
        }

        List<Checkpoint> checkpoints =
                new ArrayList<>(trip.getCheckpoints());

        checkpoints.add(new Checkpoint(index, Instant.now()));

        trip.setCheckpoints(checkpoints);

        return mapper.toResponse(repository.save(trip));
    }

    public TripResponse finish(
            String id,
            String driverId) {

        Trip trip = owned(id, driverId);

        if (trip.getStatus() != TripStatus.IN_PROGRESS) {
            throw ApiException.conflict(
                    "Only active trips can finish"
            );
        }

        if (trip.getCheckpoints().size() != trip.getStops().size()) {
            throw ApiException.conflict(
                    "Confirm all stops before finishing"
            );
        }

        trip.setActualEnd(Instant.now());
        trip.setStatus(TripStatus.COMPLETED);

        Trip saved = repository.save(trip);

        notificationEvents.tripCompleted(saved);

        return mapper.toResponse(saved);
    }

    public synchronized TripResponse cancel(
            String id,
            String accessToken) {

        Trip trip = entity(id);

        if (trip.getStatus() != TripStatus.PLANNED) {
            throw ApiException.conflict(
                    "Only planned trips can be cancelled"
            );
        }

        if (tickets.hasTickets(id, accessToken)) {
            throw ApiException.conflict(
                    "This trip has tickets and cannot be cancelled"
            );
        }

        trip.setStatus(TripStatus.CANCELLED);

        return mapper.toResponse(repository.save(trip));
    }

    public synchronized void delete(
            String id,
            String accessToken) {

        Trip trip = entity(id);

        if (trip.getStatus() == TripStatus.IN_PROGRESS) {
            throw ApiException.conflict(
                    "Finish the trip before deleting it"
            );
        }

        if (tickets.hasTickets(id, accessToken)) {
            throw ApiException.conflict(
                    "This trip has tickets and cannot be deleted"
            );
        }

        repository.delete(trip);
    }

    public List<TripResponse> active() {
        return repository.findByStatus(TripStatus.IN_PROGRESS)
                .stream()
                .map(mapper::toResponse)
                .toList();
    }
}