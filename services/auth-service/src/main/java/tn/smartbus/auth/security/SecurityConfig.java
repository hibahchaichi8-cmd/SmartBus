package tn.smartbus.auth.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.authentication.*;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
  @Bean
  JwtDecoder jwtDecoder(@Value("${app.auth-url}") String auth) {
    NimbusJwtDecoder decoder =
        NimbusJwtDecoder.withJwkSetUri(auth + "/.well-known/jwks.json")
            .jwsAlgorithm(SignatureAlgorithm.RS256)
            .build();
    decoder.setJwtValidator(
        new DelegatingOAuth2TokenValidator<>(
            JwtValidators.createDefaultWithIssuer("smartbus-auth"),
            new JwtClaimValidator<java.util.List<String>>(
                "aud", v -> v != null && v.contains("smartbus-api")),
            new JwtClaimValidator<String>("sub", v -> v != null && !v.isBlank()),
            new JwtClaimValidator<Object>("exp", v -> v != null),
            new JwtClaimValidator<Object>("iat", v -> v != null),
            new JwtClaimValidator<Object>(
                "roles",
                v ->
                    v instanceof java.util.List<?> values
                        && !values.isEmpty()
                        && values.stream()
                            .allMatch(
                                x -> java.util.Set.of("ADMIN", "CITIZEN", "DRIVER").contains(x)))));
    return decoder;
  }

  @Bean
  SecurityFilterChain security(HttpSecurity http) throws Exception {
    JwtGrantedAuthoritiesConverter roles = new JwtGrantedAuthoritiesConverter();
    roles.setAuthoritiesClaimName("roles");
    roles.setAuthorityPrefix("ROLE_");
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(roles);
    return http.csrf(c -> c.disable())
        .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(
            a ->
                a.requestMatchers("/health", "/.well-known/jwks.json", "/internal/**")
                    .permitAll()
                    .requestMatchers(HttpMethod.POST, "/api/v1/auth/register", "/api/v1/auth/login")
                    .permitAll()
                    .requestMatchers(
                        HttpMethod.GET,
                        "/api/v1/buses/**",
                        "/api/v1/stations/**",
                        "/api/v1/routes/**",
                        "/api/v1/fares/**",
                        "/api/v1/schedules/**",
                        "/api/v1/journeys")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .oauth2ResourceServer(
            o ->
                o.jwt(j -> j.jwtAuthenticationConverter(converter))
                    .authenticationEntryPoint(
                        (r, s, e) -> {
                          s.setStatus(401);
                          s.setContentType("application/json");
                          s.getWriter()
                              .write("{\"status\":401,\"message\":\"Please sign in again\"}");
                        }))
        .exceptionHandling(
            e ->
                e.accessDeniedHandler(
                    (r, s, x) -> {
                      s.setStatus(403);
                      s.setContentType("application/json");
                      s.getWriter().write("{\"status\":403,\"message\":\"Access denied\"}");
                    }))
        .build();
  }
}
