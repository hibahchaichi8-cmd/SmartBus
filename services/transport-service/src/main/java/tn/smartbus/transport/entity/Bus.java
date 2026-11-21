package tn.smartbus.transport.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("buses")
public record Bus(
    @Id String id,
    @org.springframework.data.mongodb.core.index.Indexed(unique = true) String registrationNumber,
    int capacity) {}
