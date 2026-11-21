package tn.smartbus.trip.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.smartbus.trip.dto.*;
import tn.smartbus.trip.service.ScheduleService;

@RestController
@RequestMapping("/api/v1/schedules")
public class ScheduleController {
  private final ScheduleService service;

  public ScheduleController(ScheduleService s) {
    service = s;
  }

  @GetMapping
  public List<ScheduleResponse> list() {
    return service.list();
  }

  @PutMapping("/{routeId}")
  @PreAuthorize("hasRole('ADMIN')")
  public ScheduleResponse save(
      @PathVariable String routeId, @Valid @RequestBody ScheduleRequest r) {
    return service.save(routeId, r);
  }

  @DeleteMapping("/{routeId}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> delete(@PathVariable String routeId) {
    service.delete(routeId);
    return ResponseEntity.noContent().build();
  }
}
