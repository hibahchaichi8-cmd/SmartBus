package tn.smartbus.trip.entity;

public record TripStop(
    String stationId, String name, int offsetMinutes, Double latitude, Double longitude) {}
