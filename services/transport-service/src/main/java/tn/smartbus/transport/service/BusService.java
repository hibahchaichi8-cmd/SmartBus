package tn.smartbus.transport.service;

import java.util.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import tn.smartbus.transport.dto.*;
import tn.smartbus.transport.exception.ApiException;
import tn.smartbus.transport.mapper.BusMapper;
import tn.smartbus.transport.repository.BusRepository;

@Service
public class BusService {
  private final BusRepository repository;
  private final BusMapper mapper;
  private final CatalogRules rules;

  public BusService(BusRepository r, BusMapper m, CatalogRules rules) {
    repository = r;
    mapper = m;
    this.rules = rules;
  }

  public List<BusResponse> list(int limit) {
    return repository.findAll(PageRequest.of(0, Math.max(1, Math.min(limit, 500)))).stream()
        .map(mapper::toResponse)
        .toList();
  }

  public BusResponse get(String id) {
    return mapper.toResponse(
        repository.findById(id).orElseThrow(() -> ApiException.missing("Bus")));
  }

  public BusResponse create(BusRequest r) {
    rules.validate(r);
    return mapper.toResponse(repository.insert(mapper.toEntity(UUID.randomUUID().toString(), r)));
  }

  public BusResponse update(String id, BusRequest r) {
    get(id);
    rules.beforeUpdate("buses", id);
    rules.validate(r);
    return mapper.toResponse(repository.save(mapper.toEntity(id, r)));
  }

  public void delete(String id) {
    get(id);
    rules.beforeDelete("buses", id);
    repository.deleteById(id);
  }
}
