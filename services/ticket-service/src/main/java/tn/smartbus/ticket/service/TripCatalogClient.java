package tn.smartbus.ticket.service;

import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import tn.smartbus.ticket.dto.FareDetails;
import tn.smartbus.ticket.dto.TripDetails;
import tn.smartbus.ticket.exception.ApiException;

@Component
public class TripCatalogClient {

    private final RestClient tripClient;
    private final RestClient transportClient;

    public TripCatalogClient(
            @Value("${app.trip-url}") String tripUrl,
            @Value("${app.transport-url}") String transportUrl,
            @Value("${app.internal-token}") String internalToken) {

        if (internalToken.length() < 32) {
            throw new IllegalStateException(
                    "INTERNAL_TOKEN must contain at least 32 characters"
            );
        }

        tripClient = createClient(tripUrl, internalToken);
        transportClient = createClient(transportUrl, internalToken);
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

    public TripDetails getTrip(String tripId) {

        try {
            TripDetails trip = tripClient.get()
                    .uri("/internal/trips/{id}", tripId)
                    .retrieve()
                    .body(TripDetails.class);

            if (trip == null) {
                throw new ApiException(
                        503,
                        "Trip service returned an empty response"
                );
            }

            return trip;

        } catch (HttpClientErrorException.NotFound exception) {
            throw ApiException.notFound("Trip not found");
        }
    }

    public FareDetails getFare(String routeId) {

        try {
            FareDetails fare = transportClient.get()
                    .uri("/internal/fares/{routeId}", routeId)
                    .retrieve()
                    .body(FareDetails.class);

            if (fare == null) {
                throw new ApiException(
                        503,
                        "Transport service returned an empty response"
                );
            }

            return fare;

        } catch (HttpClientErrorException.NotFound exception) {
            throw ApiException.notFound(
                    "No fare configured for this route"
            );
        }
    }
}