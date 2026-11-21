package tn.smartbus.transport.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.smartbus.transport.dto.*;
import tn.smartbus.transport.service.BusService;

@RestController
@RequestMapping("/api/v1/buses")
public class BusController {
  private final BusService service;

  public BusController(BusService s) {
    service = s;
  }

  @GetMapping
  public List<BusResponse> list(@RequestParam(defaultValue = "200") int limit) {
    return service.list(limit);
  }

  @GetMapping("/{id}")
  public BusResponse get(@PathVariable String id) {
    return service.get(id);
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<BusResponse> create(@Valid @RequestBody BusRequest r) {
    var saved = service.create(r);
    return ResponseEntity.created(URI.create("/api/v1/buses/" + saved.id())).body(saved);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public BusResponse update(@PathVariable String id, @Valid @RequestBody BusRequest r) {
    return service.update(id, r);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> delete(@PathVariable String id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }
}
