package tn.smartbus.trip.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tn.smartbus.trip.dto.CatalogDtos.*;

@Component
public class CatalogClient {
  private final ServiceClient http;
  private final String transport, auth;

  public CatalogClient(
      ServiceClient h,
      @Value("${app.transport-url}") String t,
      @Value("${app.auth-url}") String a) {
    http = h;
    transport = t;
    auth = a;
  }

  public Bus bus(String id) {
    return http.get(transport + "/internal/buses/" + safe(id), Bus.class);
  }

  public Route route(String id) {
    return http.get(transport + "/internal/routes/" + safe(id), Route.class);
  }

  public Station station(String id) {
    return http.get(transport + "/internal/stations/" + safe(id), Station.class);
  }

  public Fare fare(String id) {
    return http.get(transport + "/internal/fares/" + safe(id), Fare.class);
  }

  public Driver driver(String id) {
    return http.get(auth + "/internal/drivers/" + safe(id), Driver.class);
  }

  private String safe(String id) {
    if (id == null || !id.matches("[A-Za-z0-9-]{1,80}"))
      throw new IllegalArgumentException("Invalid identifier");
    return id;
  }
}
