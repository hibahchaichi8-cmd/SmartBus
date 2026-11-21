package tn.smartbus.notification.service;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import tn.smartbus.notification.dto.PendingTripEvent;

@Component
public class NotificationSourceClient {

    private final RestClient tripClient;
    private final RestClient ticketClient;

    public NotificationSourceClient(
            @Value("${app.trip-url}") String tripUrl,
            @Value("${app.ticket-url}") String ticketUrl,
            @Value("${app.internal-token}") String internalToken) {

        tripClient = createClient(tripUrl, internalToken);
        ticketClient = createClient(ticketUrl, internalToken);
    }

    private RestClient createClient(String url, String token) {

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();

        JdkClientHttpRequestFactory factory =
                new JdkClientHttpRequestFactory(httpClient);

        factory.setReadTimeout(Duration.ofSeconds(5));

        return RestClient.builder()
                .baseUrl(url)
                .requestFactory(factory)
                .defaultHeader("X-Service-Token", token)
                .build();
    }

    public PendingTripEvent[] getPendingEvents() {

        PendingTripEvent[] events = tripClient.get()
                .uri("/internal/notification-events")
                .retrieve()
                .body(PendingTripEvent[].class);

        if (events == null) {
            throw new IllegalStateException(
                    "Trip service returned an empty response"
            );
        }

        return events;
    }

    public List<String> getCitizenRecipients(PendingTripEvent event) {

        String[] recipients = ticketClient.get()
                .uri(
                        "/internal/tickets/recipients?tripId={id}&at={at}",
                        event.tripId(),
                        event.occurredAt().toString()
                )
                .retrieve()
                .body(String[].class);

        if (recipients == null) {
            throw new IllegalStateException(
                    "Ticket service returned an empty response"
            );
        }

        return Arrays.stream(recipients)
                .distinct()
                .toList();
    }

    public void acknowledge(String eventId) {

        tripClient.post()
                .uri(
                        "/internal/notification-events/{id}/ack",
                        eventId
                )
                .retrieve()
                .toBodilessEntity();
    }
}