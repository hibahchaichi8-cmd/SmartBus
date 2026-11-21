package tn.smartbus.transport.mapper;

import org.springframework.stereotype.Component;
import tn.smartbus.transport.dto.*;
import tn.smartbus.transport.entity.Station;

@Component
public class StationMapper {
  public Station toEntity(String id, StationRequest r) {
    return new Station(id, r.name().strip(), r.latitude(), r.longitude());
  }

  public StationResponse toResponse(Station e) {
    return new StationResponse(e.id(), e.name(), e.latitude(), e.longitude());
  }
}
