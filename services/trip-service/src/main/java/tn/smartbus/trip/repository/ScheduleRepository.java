package tn.smartbus.trip.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import tn.smartbus.trip.entity.Schedule;

public interface ScheduleRepository extends MongoRepository<Schedule, String> {}
