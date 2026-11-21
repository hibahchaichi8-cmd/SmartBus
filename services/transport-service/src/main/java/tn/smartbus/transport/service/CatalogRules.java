package tn.smartbus.transport.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tn.smartbus.transport.dto.*;
import tn.smartbus.transport.entity.*;
import tn.smartbus.transport.exception.ApiException;
import tn.smartbus.transport.repository.*;

@Service
public class CatalogRules {
  private final StationRepository stations;
  private final RouteRepository routes;
  private final FareRepository fares;
  private final ServiceClient http;
  private final String trips;

  public CatalogRules(
      StationRepository s,
      RouteRepository r,
      FareRepository f,
      ServiceClient h,
      @Value("${app.trip-url}") String t) {
    stations = s;
    routes = r;
    fares = f;
    http = h;
    trips = t;
  }

  public void validate(Object request) {
    if (request instanceof StationRequest s && ((s.latitude() == null) != (s.longitude() == null)))
      throw new ApiException(400, "Provide both coordinates or leave both empty");
    if (request instanceof FareRequest f && !routes.existsById(f.routeId()))
      throw ApiException.missing("Route");
    if (request instanceof RouteRequest r) {
      int previous = -1;
      var seen = new java.util.HashSet<String>();
      for (RouteStop stop : r.stops()) {
        if (!stations.existsById(stop.stationId())) throw ApiException.missing("Station");
        if (!seen.add(stop.stationId()))
          throw new ApiException(400, "A station may occur only once in a route");
        if (stop.offsetMinutes() <= previous)
          throw new ApiException(400, "Stop minutes must increase strictly");
        previous = stop.offsetMinutes();
      }
      if (r.stops().get(0).offsetMinutes() != 0)
        throw new ApiException(400, "The first stop offset must be zero");
    }
  }

  private boolean inUse(String type, String id) {
    return Boolean.TRUE.equals(
        http.get(trips + "/internal/references/" + type + "/" + id, Boolean.class));
  }

  public void beforeUpdate(String type, String id) {
    if ((type.equals("routes") || type.equals("buses")) && inUse(type, id))
      throw ApiException.conflict("This record is used by a schedule or an unfinished trip");
  }

  public void beforeDelete(String type, String id) {
    if (type.equals("stations")
        && routes.findAll().stream()
            .anyMatch(r -> r.stops().stream().anyMatch(s -> s.stationId().equals(id))))
      throw ApiException.conflict("Station is used by a route");
    if (type.equals("routes") && fares.findByRouteId(id).isPresent())
      throw ApiException.conflict("Delete this route's fare first");
    if ((type.equals("routes") || type.equals("buses")) && inUse(type, id))
      throw ApiException.conflict("Record is used by a schedule or an unfinished trip");
  }
}
