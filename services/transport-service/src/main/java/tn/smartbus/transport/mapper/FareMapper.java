package tn.smartbus.transport.mapper;

import org.springframework.stereotype.Component;
import tn.smartbus.transport.dto.*;
import tn.smartbus.transport.entity.Fare;

@Component
public class FareMapper {
  public Fare toEntity(String id, FareRequest r) {
    return new Fare(id, r.routeId(), r.amountMillimes());
  }

  public FareResponse toResponse(Fare e) {
    return new FareResponse(e.id(), e.routeId(), e.amountMillimes());
  }
}
