package tn.smartbus.transport.service;

import java.util.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import tn.smartbus.transport.dto.*;
import tn.smartbus.transport.exception.ApiException;
import tn.smartbus.transport.mapper.FareMapper;
import tn.smartbus.transport.repository.FareRepository;

@Service
public class FareService {
  private final FareRepository repository;
  private final FareMapper mapper;
  private final CatalogRules rules;

  public FareService(FareRepository r, FareMapper m, CatalogRules rules) {
    repository = r;
    mapper = m;
    this.rules = rules;
  }

  public List<FareResponse> list(int limit) {
    return repository.findAll(PageRequest.of(0, Math.max(1, Math.min(limit, 500)))).stream()
        .map(mapper::toResponse)
        .toList();
  }

  public FareResponse get(String id) {
    return mapper.toResponse(
        repository.findById(id).orElseThrow(() -> ApiException.missing("Fare")));
  }

  public FareResponse create(FareRequest r) {
    rules.validate(r);
    return mapper.toResponse(repository.insert(mapper.toEntity(UUID.randomUUID().toString(), r)));
  }

  public FareResponse update(String id, FareRequest r) {
    get(id);
    rules.beforeUpdate("fares", id);
    rules.validate(r);
    return mapper.toResponse(repository.save(mapper.toEntity(id, r)));
  }

  public void delete(String id) {
    get(id);
    rules.beforeDelete("fares", id);
    repository.deleteById(id);
  }
}
