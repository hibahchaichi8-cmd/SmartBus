package tn.smartbus.transport.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import tn.smartbus.transport.dto.*;
import tn.smartbus.transport.service.FareService;

@RestController
@RequestMapping("/api/v1/fares")
public class FareController {
  private final FareService service;

  public FareController(FareService s) {
    service = s;
  }

  @GetMapping
  public List<FareResponse> list(@RequestParam(defaultValue = "200") int limit) {
    return service.list(limit);
  }

  @GetMapping("/{id}")
  public FareResponse get(@PathVariable String id) {
    return service.get(id);
  }

  @PostMapping
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<FareResponse> create(@Valid @RequestBody FareRequest r) {
    var saved = service.create(r);
    return ResponseEntity.created(URI.create("/api/v1/fares/" + saved.id())).body(saved);
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public FareResponse update(@PathVariable String id, @Valid @RequestBody FareRequest r) {
    return service.update(id, r);
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<Void> delete(@PathVariable String id) {
    service.delete(id);
    return ResponseEntity.noContent().build();
  }
}
