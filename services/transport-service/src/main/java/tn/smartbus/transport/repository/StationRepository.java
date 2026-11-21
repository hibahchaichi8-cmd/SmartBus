package tn.smartbus.transport.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import tn.smartbus.transport.entity.Station;

public interface StationRepository extends MongoRepository<Station, String> {}
