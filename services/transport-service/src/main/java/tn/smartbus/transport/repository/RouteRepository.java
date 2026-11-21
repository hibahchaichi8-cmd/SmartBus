package tn.smartbus.transport.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import tn.smartbus.transport.entity.Route;

public interface RouteRepository extends MongoRepository<Route, String> {}
