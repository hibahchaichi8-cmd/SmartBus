package tn.smartbus.trip.service;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import tn.smartbus.trip.entity.Schedule;
import tn.smartbus.trip.repository.ScheduleRepository;

@Component
public class SeedData implements CommandLineRunner {
  private final ScheduleRepository schedules;
  private final MongoTemplate mongo;
  private final boolean enabled;

  public SeedData(ScheduleRepository s, MongoTemplate m, @Value("${app.seed}") boolean e) {
    schedules = s;
    mongo = m;
    enabled = e;
  }

  public void run(String... args) {
    if (!enabled
        || mongo.getCollection("metadata").find(new Document("_id", "seed-v1")).first() != null)
      return;
    schedules.save(new Schedule("route-20", 360, 1320, 20));
    schedules.save(new Schedule("route-5", 360, 1320, 30));
    mongo.getCollection("metadata").insertOne(new Document("_id", "seed-v1"));
  }
}
