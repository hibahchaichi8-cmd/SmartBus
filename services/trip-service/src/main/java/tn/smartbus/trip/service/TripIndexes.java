package tn.smartbus.trip.service;

import com.mongodb.client.model.IndexOptions;
import org.bson.Document;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
public class TripIndexes implements CommandLineRunner {
  private final MongoTemplate mongo;

  public TripIndexes(MongoTemplate m) {
    mongo = m;
  }

  public void run(String... args) {
    for (String field : java.util.List.of("busId", "driverId"))
      mongo
          .getCollection("trips")
          .createIndex(
              new Document(field, 1),
              new IndexOptions()
                  .unique(true)
                  .name("one_active_" + field)
                  .partialFilterExpression(new Document("status", "IN_PROGRESS")));
  }
}
