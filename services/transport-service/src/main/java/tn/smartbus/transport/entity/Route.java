package tn.smartbus.transport.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("routes")
public record Route(
    @Id String id,
    @org.springframework.data.mongodb.core.index.Indexed(unique = true) String code,
    String name,
    java.util.List<RouteStop> stops) {}
