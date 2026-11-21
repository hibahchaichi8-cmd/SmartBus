package tn.smartbus.trip.service;

import java.time.*;
import java.util.*;
import org.springframework.stereotype.Service;
import tn.smartbus.trip.dto.*;
import tn.smartbus.trip.exception.ApiException;
import tn.smartbus.trip.repository.ScheduleRepository;

@Service
public class JourneyService {
  private final ScheduleRepository schedules;
  private final CatalogClient catalog;

  public JourneyService(ScheduleRepository s, CatalogClient c) {
    schedules = s;
    catalog = c;
  }

  public List<JourneyResponse> search(String origin, String destination, LocalDateTime local) {
    if (origin.equals(destination))
      throw new ApiException(400, "Choose different origin and destination stations");
    catalog.station(origin);
    catalog.station(destination);
    ZoneId zone = ZoneId.of("Africa/Tunis");
    Instant desired = local.atZone(zone).toInstant();
    var result = new ArrayList<JourneyResponse>();
    for (var schedule : schedules.findAll()) {
      var route = catalog.route(schedule.routeId());
      int from = -1, to = -1;
      for (int i = 0; i < route.stops().size(); i++) {
        String station = route.stops().get(i).stationId();
        if (station.equals(origin)) from = i;
        if (station.equals(destination)) to = i;
      }
      if (from < 0 || to <= from) continue;
      var first = route.stops().get(from);
      var last = route.stops().get(to);
      Instant base = local.toLocalDate().atStartOfDay(zone).toInstant();
      long requested =
          (long) Math.ceil((desired.getEpochSecond() - base.getEpochSecond()) / 60.0)
              - first.offsetMinutes();
      long index =
          Math.max(
              0,
              (long)
                  Math.ceil(
                      (requested - schedule.firstMinute()) / (double) schedule.intervalMinutes()));
      long minute = schedule.firstMinute() + index * schedule.intervalMinutes();
      if (minute > schedule.lastMinute()) continue;
      tn.smartbus.trip.dto.CatalogDtos.Fare fare;
      try {
        fare = catalog.fare(route.id());
      } catch (ApiException error) {
        if (error.status == 404) continue; // Unpriced routes are not yet available to citizens.
        throw error;
      }
      Instant departure = base.plusSeconds((minute + first.offsetMinutes()) * 60L);
      int duration = last.offsetMinutes() - first.offsetMinutes();
      result.add(
          new JourneyResponse(
              route.id(),
              route.code() + " · " + route.name(),
              catalog.station(origin).name(),
              catalog.station(destination).name(),
              departure,
              departure.plusSeconds(duration * 60L),
              duration,
              fare.amountMillimes(),
              "TND",
              "TIMETABLE"));
    }
    return result.stream()
        .sorted(Comparator.comparing(JourneyResponse::departure))
        .limit(50)
        .toList();
  }
}
