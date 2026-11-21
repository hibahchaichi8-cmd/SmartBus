package tn.smartbus.auth.security;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import java.time.Instant;
import java.util.*;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import tn.smartbus.auth.dto.AuthResponse;
import tn.smartbus.auth.entity.User;
import tn.smartbus.auth.mapper.UserMapper;

@Service
public class TokenService {
  private final JwtEncoder encoder;
  private final String kid;
  private final UserMapper mapper;

  public TokenService(SigningKeyProvider keys, UserMapper mapper) {
    this.mapper = mapper;
    kid = keys.signingKey().getKeyID();
    encoder = new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(keys.signingKey())));
  }

  public AuthResponse issue(User user) {
    Instant now = Instant.now(), expires = now.plusSeconds(1800);
    var claims =
        JwtClaimsSet.builder()
            .issuer("smartbus-auth")
            .audience(List.of("smartbus-api"))
            .subject(user.id())
            .issuedAt(now)
            .expiresAt(expires)
            .id(UUID.randomUUID().toString())
            .claim("roles", List.of(user.role().name()))
            .build();
    String token =
        encoder
            .encode(
                JwtEncoderParameters.from(
                    JwsHeader.with(SignatureAlgorithm.RS256).keyId(kid).build(), claims))
            .getTokenValue();
    return new AuthResponse(token, "Bearer", expires, mapper.toResponse(user));
  }
}
