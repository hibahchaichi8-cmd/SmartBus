package tn.smartbus.trip.service;

import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import tn.smartbus.trip.exception.ApiException;

@Component
public class TicketClient {

    private final RestClient client;

    public TicketClient(
            @Value("${app.ticket-url}") String ticketUrl) {

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();

        JdkClientHttpRequestFactory factory =
                new JdkClientHttpRequestFactory(httpClient);

        factory.setReadTimeout(Duration.ofSeconds(5));

        client = RestClient.builder()
                .baseUrl(ticketUrl)
                .requestFactory(factory)
                .build();
    }

    public boolean hasTickets(String tripId, String accessToken) {

        Boolean result = client.get()
                .uri(
                        "/api/v1/admin/tickets/exists?tripId={id}",
                        tripId
                )
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(Boolean.class);

        if (result == null) {
            throw new ApiException(
                    503,
                    "Unable to check trip tickets"
            );
        }

        return result;
    }
}