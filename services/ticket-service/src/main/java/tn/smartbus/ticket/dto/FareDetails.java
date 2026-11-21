package tn.smartbus.ticket.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record FareDetails(
        String id,
        String routeId,
        int amountMillimes
) {
}