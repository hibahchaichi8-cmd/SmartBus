package tn.smartbus.transport.service;

import java.util.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import tn.smartbus.transport.dto.*;
import tn.smartbus.transport.exception.ApiException;
import tn.smartbus.transport.mapper.RouteMapper;
import tn.smartbus.transport.repository.RouteRepository;

@Service
public class RouteService {
  private final RouteRepository repository;
  private final RouteMapper mapper;
  private final CatalogRules rules;

  public RouteService(RouteRepository r, RouteMapper m, CatalogRules rules) {
    repository = r;
    mapper = m;
    this.rules = rules;
  }

  public List<RouteResponse> list(int limit) {
    return repository.findAll(PageRequest.of(0, Math.max(1, Math.min(limit, 500)))).stream()
        .map(mapper::toResponse)
        .toList();
  }

  public RouteResponse get(String id) {
    return mapper.toResponse(
        repository.findById(id).orElseThrow(() -> ApiException.missing("Route")));
  }

  public RouteResponse create(RouteRequest r) {
    rules.validate(r);
    return mapper.toResponse(repository.insert(mapper.toEntity(UUID.randomUUID().toString(), r)));
  }

  public RouteResponse update(String id, RouteRequest r) {
    get(id);
    rules.beforeUpdate("routes", id);
    rules.validate(r);
    return mapper.toResponse(repository.save(mapper.toEntity(id, r)));
  }

  public void delete(String id) {
    get(id);
    rules.beforeDelete("routes", id);
    repository.deleteById(id);
  }
}
