package tn.smartbus.trip.mapper;

import java.time.Instant;
import org.springframework.stereotype.Component;
import tn.smartbus.trip.dto.TripResponse;
import tn.smartbus.trip.entity.*;

@Component
public class TripMapper {
  public TripResponse toResponse(Trip t) {
    int duration = t.getStops().get(t.getStops().size() - 1).offsetMinutes();
    Instant eta = t.getPlannedDeparture().plusSeconds(duration * 60L);
    if (t.getActualStart() != null) eta = t.getActualStart().plusSeconds(duration * 60L);
    if (!t.getCheckpoints().isEmpty()) {
      Checkpoint last = t.getCheckpoints().get(t.getCheckpoints().size() - 1);
      eta =
          last.arrivedAt()
              .plusSeconds((duration - t.getStops().get(last.stopIndex()).offsetMinutes()) * 60L);
    }
    if (t.getActualEnd() != null) eta = t.getActualEnd();
    return new TripResponse(
        t.getId(),
        t.getRouteId(),
        t.getRouteName(),
        t.getBusId(),
        t.getRegistrationNumber(),
        t.getCapacity(),
        t.getDriverId(),
        t.getDriverName(),
        t.getPlannedDeparture(),
        t.getActualStart(),
        t.getActualEnd(),
        t.getStatus(),
        t.getStops(),
        t.getCheckpoints(),
        eta,
        t.getStatus() == TripStatus.IN_PROGRESS && eta.isBefore(Instant.now()));
  }
}
