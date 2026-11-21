package tn.smartbus.notification.security;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    private static final Set<String> ALLOWED_ROLES =
            Set.of("ADMIN", "CITIZEN", "DRIVER");

    @Bean
    public JwtDecoder jwtDecoder(
            @Value("${app.auth-url}") String authUrl) {

        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withJwkSetUri(authUrl + "/.well-known/jwks.json")
                .jwsAlgorithm(SignatureAlgorithm.RS256)
                .build();

        JwtClaimValidator<Object> audienceValidator =
                new JwtClaimValidator<>(
                        "aud",
                        value -> value instanceof List<?> audiences
                                && audiences.contains("smartbus-api")
                );

        JwtClaimValidator<Object> subjectValidator =
                new JwtClaimValidator<>(
                        "sub",
                        value -> value instanceof String subject
                                && !subject.isBlank()
                );

        JwtClaimValidator<Object> expirationRequired =
                new JwtClaimValidator<>("exp", value -> value != null);

        JwtClaimValidator<Object> issuedAtRequired =
                new JwtClaimValidator<>("iat", value -> value != null);

        JwtClaimValidator<Object> rolesValidator =
                new JwtClaimValidator<>(
                        "roles",
                        value -> value instanceof List<?> roles
                                && !roles.isEmpty()
                                && roles.stream().allMatch(
                                        role -> role instanceof String name
                                                && ALLOWED_ROLES.contains(name)
                                )
                );

        decoder.setJwtValidator(
                new DelegatingOAuth2TokenValidator<>(
                        JwtValidators.createDefaultWithIssuer("smartbus-auth"),
                        audienceValidator,
                        subjectValidator,
                        expirationRequired,
                        issuedAtRequired,
                        rolesValidator
                )
        );

        return decoder;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            @Value("${app.internal-token}") String internalToken)
            throws Exception {

        JwtGrantedAuthoritiesConverter rolesConverter =
                new JwtGrantedAuthoritiesConverter();

        rolesConverter.setAuthoritiesClaimName("roles");
        rolesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter authenticationConverter =
                new JwtAuthenticationConverter();

        authenticationConverter.setJwtGrantedAuthoritiesConverter(
                rolesConverter
        );

        http
                .csrf(csrf -> csrf.disable())

                .sessionManagement(session -> session
                        .sessionCreationPolicy(
                                SessionCreationPolicy.STATELESS
                        )
                )

                .addFilterBefore(
                        new InternalTokenFilter(internalToken),
                        BearerTokenAuthenticationFilter.class
                )

                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.GET, "/health")
                        .permitAll()

                        .requestMatchers(
                                HttpMethod.POST,
                                "/internal/notifications"
                        )
                        .permitAll()

                        .requestMatchers(
                                HttpMethod.GET,
                                "/api/v1/notifications",
                                "/api/v1/notifications/unread-count"
                        )
                        .authenticated()

                        .requestMatchers(
                                HttpMethod.PATCH,
                                "/api/v1/notifications/*/read"
                        )
                        .authenticated()

                        .anyRequest()
                        .denyAll()
                )

                .oauth2ResourceServer(resourceServer -> resourceServer
                        .jwt(jwt -> jwt
                                .jwtAuthenticationConverter(
                                        authenticationConverter
                                )
                        )
                        .authenticationEntryPoint(
                                (request, response, exception) -> {
                                    response.setStatus(401);
                                    response.setHeader(
                                            "WWW-Authenticate",
                                            "Bearer"
                                    );
                                    response.setContentType(
                                            "application/json"
                                    );
                                    response.setCharacterEncoding("UTF-8");

                                    response.getWriter().write(
                                            "{\"status\":401,"
                                            + "\"message\":\"Please sign in again\"}"
                                    );
                                }
                        )
                )

                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedHandler(
                                (request, response, exception) -> {
                                    response.setStatus(403);
                                    response.setContentType(
                                            "application/json"
                                    );
                                    response.setCharacterEncoding("UTF-8");

                                    response.getWriter().write(
                                            "{\"status\":403,"
                                            + "\"message\":\"Access denied\"}"
                                    );
                                }
                        )
                );

        return http.build();
    }
}