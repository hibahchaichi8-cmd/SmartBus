package tn.smartbus.trip.dto;

import java.util.List;

public final class CatalogDtos {
  private CatalogDtos() {}

  public record Bus(String id, String registrationNumber, int capacity) {}

  public record Station(String id, String name, Double latitude, Double longitude) {}

  public record Stop(String stationId, int offsetMinutes) {}

  public record Route(String id, String code, String name, List<Stop> stops) {}

  public record Fare(String id, String routeId, int amountMillimes) {}

  public record Driver(String id, String fullName, String email, String role) {}
}
