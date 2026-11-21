package tn.smartbus.trip.controller;

import java.time.*;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import tn.smartbus.trip.dto.JourneyResponse;
import tn.smartbus.trip.service.JourneyService;

@RestController
public class JourneyController {
  private final JourneyService service;

  public JourneyController(JourneyService s) {
    service = s;
  }

  @GetMapping("/api/v1/journeys")
  public List<JourneyResponse> search(
      @RequestParam String originId,
      @RequestParam String destinationId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime departure) {
    return service.search(originId, destinationId, departure);
  }
}
