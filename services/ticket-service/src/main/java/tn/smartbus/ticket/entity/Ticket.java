package tn.smartbus.ticket.entity;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Document(collection = "tickets")
@CompoundIndex(
        name = "unique_citizen_purchase_request",
        def = "{'citizenId': 1, 'purchaseRequestId': 1}",
        unique = true
)
public record Ticket(

        @Id
        String id,

        @Version
        Long version,

        @Indexed
        String citizenId,

        @Indexed
        String tripId,

        String routeName,

        String registrationNumber,

        Instant plannedDeparture,

        int amountMillimes,

        String currency,

        TicketStatus status,

        Instant purchasedAt,

        Instant validatedAt,

        String validatedBy,

        @JsonIgnore
        @Indexed(unique = true)
        String qrTokenHash,

        String purchaseRequestId

) {
}