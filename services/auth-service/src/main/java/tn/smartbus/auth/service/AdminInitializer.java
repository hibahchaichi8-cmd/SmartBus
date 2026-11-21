package tn.smartbus.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import tn.smartbus.auth.dto.RegisterRequest;
import tn.smartbus.auth.entity.UserRole;
import tn.smartbus.auth.repository.UserRepository;

@Component
public class AdminInitializer implements CommandLineRunner {
  private final AuthService service;
  private final UserRepository users;
  private final String email, password;

  public AdminInitializer(
      AuthService service,
      UserRepository users,
      @Value("${ADMIN_EMAIL}") String email,
      @Value("${ADMIN_PASSWORD}") String password) {
    this.service = service;
    this.users = users;
    this.email = email;
    this.password = password;
  }

  public void run(String... args) {
    if (password.startsWith("REPLACE_") || password.length() < 12)
      throw new IllegalStateException("ADMIN_PASSWORD must have at least 12 characters");
    if (users.findByEmail(email.strip().toLowerCase(java.util.Locale.ROOT)).isEmpty())
      service.create(new RegisterRequest("Administrator", email, password), UserRole.ADMIN);
  }
}
