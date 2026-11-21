package tn.smartbus.transport.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("stations")
public record Station(@Id String id, String name, Double latitude, Double longitude) {}
