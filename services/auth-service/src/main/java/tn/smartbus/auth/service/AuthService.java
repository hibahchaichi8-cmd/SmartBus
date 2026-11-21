package tn.smartbus.auth.service;

import java.nio.charset.StandardCharsets;
import java.util.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import tn.smartbus.auth.dto.*;
import tn.smartbus.auth.entity.*;
import tn.smartbus.auth.exception.ApiException;
import tn.smartbus.auth.mapper.UserMapper;
import tn.smartbus.auth.repository.UserRepository;
import tn.smartbus.auth.security.TokenService;

@Service
public class AuthService {
  private final UserRepository users;
  private final PasswordEncoder passwords;
  private final TokenService tokens;
  private final UserMapper mapper;

  public AuthService(
      UserRepository users, PasswordEncoder passwords, TokenService tokens, UserMapper mapper) {
    this.users = users;
    this.passwords = passwords;
    this.tokens = tokens;
    this.mapper = mapper;
  }

  public UserResponse create(RegisterRequest r, UserRole role) {
    if (r.password().getBytes(StandardCharsets.UTF_8).length > 72)
      throw new ApiException(400, "Password must contain at most 72 UTF-8 bytes");
    String email = r.email().strip().toLowerCase(Locale.ROOT);
    if (users.findByEmail(email).isPresent())
      throw ApiException.conflict("Email is already registered");
    return mapper.toResponse(
        users.save(
            new User(
                UUID.randomUUID().toString(),
                r.fullName().strip(),
                email,
                passwords.encode(r.password()),
                role)));
  }

  public AuthResponse register(RegisterRequest request) {
    var user = create(request, UserRole.CITIZEN);
    return tokens.issue(users.findById(user.id()).orElseThrow());
  }

  public AuthResponse login(LoginRequest r) {
    User u = users.findByEmail(r.email().strip().toLowerCase(Locale.ROOT)).orElse(null);
    if (u == null || !passwords.matches(r.password(), u.passwordHash()))
      throw new ApiException(401, "Incorrect email or password");
    return tokens.issue(u);
  }

  public UserResponse me(String id) {
    return mapper.toResponse(users.findById(id).orElseThrow(() -> ApiException.missing("User")));
  }

  public List<UserResponse> drivers() {
    return users.findAll().stream()
        .filter(u -> u.role() == UserRole.DRIVER)
        .map(mapper::toResponse)
        .toList();
  }

  public UserResponse driver(String id) {
    User u = users.findById(id).orElseThrow(() -> ApiException.missing("Driver"));
    if (u.role() != UserRole.DRIVER) throw ApiException.missing("Driver");
    return mapper.toResponse(u);
  }
}
