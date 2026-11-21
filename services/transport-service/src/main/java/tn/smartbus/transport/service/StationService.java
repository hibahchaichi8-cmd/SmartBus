package tn.smartbus.transport.service;

import java.util.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import tn.smartbus.transport.dto.*;
import tn.smartbus.transport.exception.ApiException;
import tn.smartbus.transport.mapper.StationMapper;
import tn.smartbus.transport.repository.StationRepository;

@Service
public class StationService {
  private final StationRepository repository;
  private final StationMapper mapper;
  private final CatalogRules rules;

  public StationService(StationRepository r, StationMapper m, CatalogRules rules) {
    repository = r;
    mapper = m;
    this.rules = rules;
  }

  public List<StationResponse> list(int limit) {
    return repository.findAll(PageRequest.of(0, Math.max(1, Math.min(limit, 500)))).stream()
        .map(mapper::toResponse)
        .toList();
  }

  public StationResponse get(String id) {
    return mapper.toResponse(
        repository.findById(id).orElseThrow(() -> ApiException.missing("Station")));
  }

  public StationResponse create(StationRequest r) {
    rules.validate(r);
    return mapper.toResponse(repository.insert(mapper.toEntity(UUID.randomUUID().toString(), r)));
  }

  public StationResponse update(String id, StationRequest r) {
    get(id);
    rules.beforeUpdate("stations", id);
    rules.validate(r);
    return mapper.toResponse(repository.save(mapper.toEntity(id, r)));
  }

  public void delete(String id) {
    get(id);
    rules.beforeDelete("stations", id);
    repository.deleteById(id);
  }
}
