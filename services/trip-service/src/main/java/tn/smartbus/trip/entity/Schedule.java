package tn.smartbus.trip.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("schedules")
public record Schedule(@Id String routeId, int firstMinute, int lastMinute, int intervalMinutes) {}
