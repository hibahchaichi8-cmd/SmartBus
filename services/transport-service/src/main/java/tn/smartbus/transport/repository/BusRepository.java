package tn.smartbus.transport.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import tn.smartbus.transport.entity.Bus;

public interface BusRepository extends MongoRepository<Bus, String> {}
