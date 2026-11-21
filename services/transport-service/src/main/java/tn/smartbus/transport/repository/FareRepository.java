package tn.smartbus.transport.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import tn.smartbus.transport.entity.Fare;

public interface FareRepository extends MongoRepository<Fare, String> {
  java.util.Optional<Fare> findByRouteId(String routeId);
}
