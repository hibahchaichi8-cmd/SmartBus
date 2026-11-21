package tn.smartbus.auth.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document("users")
public record User(
    @Id String id,
    String fullName,
    @Indexed(unique = true) String email,
    @com.fasterxml.jackson.annotation.JsonIgnore String passwordHash,
    UserRole role) {}
