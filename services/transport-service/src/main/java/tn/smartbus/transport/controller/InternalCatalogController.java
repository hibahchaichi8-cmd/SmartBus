package tn.smartbus.transport.controller;

import org.springframework.web.bind.annotation.*;
import tn.smartbus.transport.entity.*;
import tn.smartbus.transport.exception.ApiException;
import tn.smartbus.transport.repository.*;

@RestController
@RequestMapping("/internal")
public class InternalCatalogController {
  private final BusRepository buses;
  private final StationRepository stations;
  private final RouteRepository routes;
  private final FareRepository fares;

  public InternalCatalogController(
      BusRepository b, StationRepository s, RouteRepository r, FareRepository f) {
    buses = b;
    stations = s;
    routes = r;
    fares = f;
  }

  @GetMapping("/buses/{id}")
  public Bus bus(@PathVariable String id) {
    return buses.findById(id).orElseThrow(() -> ApiException.missing("Bus"));
  }

  @GetMapping("/stations/{id}")
  public Station station(@PathVariable String id) {
    return stations.findById(id).orElseThrow(() -> ApiException.missing("Station"));
  }

  @GetMapping("/routes/{id}")
  public Route route(@PathVariable String id) {
    return routes.findById(id).orElseThrow(() -> ApiException.missing("Route"));
  }

  @GetMapping("/fares/{routeId}")
  public Fare fare(@PathVariable String routeId) {
    return fares.findByRouteId(routeId).orElseThrow(() -> ApiException.missing("Fare"));
  }
}
