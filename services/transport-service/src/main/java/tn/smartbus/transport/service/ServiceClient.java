package tn.smartbus.transport.service;

import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class ServiceClient {
  private final RestClient client;

  public ServiceClient(@Value("${app.internal-token}") String token) {
    var factory =
        new JdkClientHttpRequestFactory(
            HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build());
    factory.setReadTimeout(Duration.ofSeconds(5));
    client =
        RestClient.builder()
            .requestFactory(factory)
            .defaultHeader("X-Service-Token", token)
            .build();
  }

  public <T> T get(String url, Class<T> type) {
    try {
      return client.get().uri(url).retrieve().body(type);
    } catch (org.springframework.web.client.HttpClientErrorException.NotFound e) {
      throw new tn.smartbus.transport.exception.ApiException(404, "Referenced record not found");
    }
  }
}
