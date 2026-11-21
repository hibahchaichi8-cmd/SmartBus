package tn.smartbus.transport.mapper;

import org.springframework.stereotype.Component;
import tn.smartbus.transport.dto.*;
import tn.smartbus.transport.entity.Route;

@Component
public class RouteMapper {
  public Route toEntity(String id, RouteRequest r) {
    return new Route(id, r.code().strip(), r.name().strip(), java.util.List.copyOf(r.stops()));
  }

  public RouteResponse toResponse(Route e) {
    return new RouteResponse(e.id(), e.code(), e.name(), e.stops());
  }
}
