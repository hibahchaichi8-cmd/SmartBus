package tn.smartbus.transport.mapper;

import org.springframework.stereotype.Component;
import tn.smartbus.transport.dto.*;
import tn.smartbus.transport.entity.Bus;

@Component
public class BusMapper {
  public Bus toEntity(String id, BusRequest r) {
    return new Bus(
        id, r.registrationNumber().strip().toUpperCase(java.util.Locale.ROOT), r.capacity());
  }

  public BusResponse toResponse(Bus e) {
    return new BusResponse(e.id(), e.registrationNumber(), e.capacity());
  }
}
