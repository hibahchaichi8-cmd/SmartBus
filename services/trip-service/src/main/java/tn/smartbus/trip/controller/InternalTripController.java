package tn.smartbus.trip.controller;

import java.util.List;
import org.springframework.web.bind.annotation.*;
import tn.smartbus.trip.dto.TripResponse;
import tn.smartbus.trip.entity.TripStatus;
import tn.smartbus.trip.repository.*;
import tn.smartbus.trip.service.TripService;

@RestController
@RequestMapping("/internal")
public class InternalTripController {
  private final tn.smartbus.trip.mapper.TripMapper mapper;
  private final TripService service;
  private final TripRepository trips;
  private final ScheduleRepository schedules;

  public InternalTripController(
      TripService s, TripRepository t, ScheduleRepository r, tn.smartbus.trip.mapper.TripMapper m) {
    mapper = m;
    service = s;
    trips = t;
    schedules = r;
  }

  @GetMapping("/trips/active")
  public List<TripResponse> active() {
    return service.active();
  }

  @GetMapping("/trips/{id}")
  public TripResponse trip(@PathVariable String id) {
    return mapper.toResponse(service.entity(id));
  }

  @GetMapping("/references/{type}/{id}")
  public boolean inUse(@PathVariable String type, @PathVariable String id) {
    var unfinished = List.of(TripStatus.PLANNED, TripStatus.IN_PROGRESS);
    return type.equals("routes")
        ? (schedules.existsById(id) || trips.existsByRouteIdAndStatusIn(id, unfinished))
        : type.equals("buses") && trips.existsByBusIdAndStatusIn(id, unfinished);
  }
}
