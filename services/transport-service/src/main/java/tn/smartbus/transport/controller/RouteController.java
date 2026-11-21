package tn.smartbus.transport.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.smartbus.transport.dto.*;
import tn.smartbus.transport.service.RouteService;

@RestController
@RequestMapping("/api/v1/routes")
public class RouteController {
  private final RouteService service;

  public RouteController(RouteService s) {
    service = s;
  }

  @GetMapping
  public List<RouteResponse> list(@RequestParam(defaultValue = "200") int limit) {
    return service.list(limit);
  }

  @GetMapping("/{id}")
  public RouteResponse get(@PathVariable String id) {
    return service.get(id);
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<RouteResponse> create(@Valid @RequestBody RouteRequest r) {
    var saved = service.create(r);
    return ResponseEntity.created(URI.create("/api/v1/routes/" + saved.id())).body(saved);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public RouteResponse update(@PathVariable String id, @Valid @RequestBody RouteRequest r) {
    return service.update(id, r);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> delete(@PathVariable String id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }
}
