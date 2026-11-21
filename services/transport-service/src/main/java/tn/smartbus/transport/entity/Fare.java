package tn.smartbus.transport.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("fares")
public record Fare(
    @Id String id,
    @org.springframework.data.mongodb.core.index.Indexed(unique = true) String routeId,
    int amountMillimes) {}
