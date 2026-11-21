package tn.smartbus.trip.service;

import java.util.List;
import org.springframework.stereotype.Service;
import tn.smartbus.trip.dto.*;
import tn.smartbus.trip.exception.ApiException;
import tn.smartbus.trip.mapper.ScheduleMapper;
import tn.smartbus.trip.repository.ScheduleRepository;

@Service
public class ScheduleService {
  private final ScheduleRepository repository;
  private final ScheduleMapper mapper;
  private final CatalogClient catalog;

  public ScheduleService(ScheduleRepository r, ScheduleMapper m, CatalogClient c) {
    repository = r;
    mapper = m;
    catalog = c;
  }

  public List<ScheduleResponse> list() {
    return repository.findAll().stream().map(mapper::toResponse).toList();
  }

  public ScheduleResponse save(String id, ScheduleRequest r) {
    catalog.route(id);
    return mapper.toResponse(repository.save(mapper.toEntity(id, r)));
  }

  public void delete(String id) {
    if (!repository.existsById(id)) throw ApiException.missing("Schedule");
    repository.deleteById(id);
  }
}
