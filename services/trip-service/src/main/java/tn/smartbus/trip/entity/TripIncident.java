package tn.smartbus.trip.entity;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "trip_incidents")
public record TripIncident(

        @Id
        String id,

        @Indexed
        String tripId,

        String driverId,

        TripIncidentType type,

        String routeName,

        String registrationNumber,

        Integer delayMinutes,

        Instant reportedAt

) {
}