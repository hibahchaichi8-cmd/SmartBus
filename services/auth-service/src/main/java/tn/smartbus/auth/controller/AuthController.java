package tn.smartbus.auth.controller;

import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import tn.smartbus.auth.dto.*;
import tn.smartbus.auth.security.SigningKeyProvider;
import tn.smartbus.auth.service.AuthService;

@RestController
public class AuthController {
  private final AuthService service;
  private final SigningKeyProvider keys;

  public AuthController(AuthService service, SigningKeyProvider keys) {
    this.service = service;
    this.keys = keys;
  }

  @PostMapping("/api/v1/auth/register")
  public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest r) {
    return ResponseEntity.status(201).body(service.register(r));
  }

  @PostMapping("/api/v1/auth/login")
  public AuthResponse login(@Valid @RequestBody LoginRequest r) {
    return service.login(r);
  }

  @GetMapping("/api/v1/auth/me")
  public UserResponse me(@AuthenticationPrincipal Jwt jwt) {
    return service.me(jwt.getSubject());
  }

  @GetMapping("/.well-known/jwks.json")
  public java.util.Map<String, Object> keys() {
    return keys.publicJwks();
  }
}
