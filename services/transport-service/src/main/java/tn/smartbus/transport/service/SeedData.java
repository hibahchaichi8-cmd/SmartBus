package tn.smartbus.transport.service;

import java.util.List;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;
import tn.smartbus.transport.entity.*;
import tn.smartbus.transport.repository.*;

@Component
public class SeedData implements CommandLineRunner {
  private final BusRepository buses;
  private final StationRepository stations;
  private final RouteRepository routes;
  private final FareRepository fares;
  private final MongoTemplate mongo;
  private final boolean enabled;

  public SeedData(
      BusRepository b,
      StationRepository s,
      RouteRepository r,
      FareRepository f,
      MongoTemplate m,
      @Value("${app.seed}") boolean e) {
    buses = b;
    stations = s;
    routes = r;
    fares = f;
    mongo = m;
    enabled = e;
  }

  public void run(String... args) {
    if (!enabled
        || mongo.getCollection("metadata").find(new Document("_id", "seed-v1")).first() != null)
      return;
    stations.save(new Station("station-tunis", "Tunis Marine", 36.8008, 10.1914));
    stations.save(new Station("station-bardo", "Le Bardo", 36.8092, 10.1406));
    stations.save(new Station("station-manouba", "Manouba", 36.8101, 10.0973));
    stations.save(new Station("station-ariana", "Ariana", 36.8665, 10.1647));
    buses.save(new Bus("bus-1001", "TN-1001", 50));
    buses.save(new Bus("bus-1002", "TN-1002", 40));
    routes.save(
        new Route(
            "route-20",
            "20",
            "Tunis – Manouba",
            List.of(
                new RouteStop("station-tunis", 0),
                new RouteStop("station-bardo", 15),
                new RouteStop("station-manouba", 35))));
    routes.save(
        new Route(
            "route-5",
            "5",
            "Tunis – Ariana",
            List.of(new RouteStop("station-tunis", 0), new RouteStop("station-ariana", 30))));
    fares.save(new Fare("fare-20", "route-20", 1200));
    fares.save(new Fare("fare-5", "route-5", 1000));
    mongo.getCollection("metadata").insertOne(new Document("_id", "seed-v1"));
  }
}
