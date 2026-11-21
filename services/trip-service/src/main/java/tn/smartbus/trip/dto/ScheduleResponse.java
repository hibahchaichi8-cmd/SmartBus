package tn.smartbus.trip.dto;

public record ScheduleResponse(
    String routeId, String firstDeparture, String lastDeparture, int intervalMinutes) {}
