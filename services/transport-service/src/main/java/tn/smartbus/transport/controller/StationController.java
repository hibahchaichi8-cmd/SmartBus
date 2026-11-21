package tn.smartbus.transport.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.smartbus.transport.dto.*;
import tn.smartbus.transport.service.StationService;

@RestController
@RequestMapping("/api/v1/stations")
public class StationController {
  private final StationService service;

  public StationController(StationService s) {
    service = s;
  }

  @GetMapping
  public List<StationResponse> list(@RequestParam(defaultValue = "200") int limit) {
    return service.list(limit);
  }

  @GetMapping("/{id}")
  public StationResponse get(@PathVariable String id) {
    return service.get(id);
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<StationResponse> create(@Valid @RequestBody StationRequest r) {
    var saved = service.create(r);
    return ResponseEntity.created(URI.create("/api/v1/stations/" + saved.id())).body(saved);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public StationResponse update(@PathVariable String id, @Valid @RequestBody StationRequest r) {
    return service.update(id, r);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> delete(@PathVariable String id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }
}
