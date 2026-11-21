package tn.smartbus.auth;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import tn.smartbus.auth.dto.*;
import tn.smartbus.auth.entity.*;
import tn.smartbus.auth.mapper.UserMapper;
import tn.smartbus.auth.repository.UserRepository;
import tn.smartbus.auth.security.TokenService;
import tn.smartbus.auth.service.AuthService;

class AuthServiceTest {
  @Test
  void registrationHashesAndReturnsNoPassword() {
    var repo = mock(UserRepository.class);
    when(repo.findByEmail(anyString())).thenReturn(Optional.empty());
    when(repo.save(any())).thenAnswer(i -> i.getArgument(0));
    var encoder = new BCryptPasswordEncoder(4);
    var service = new AuthService(repo, encoder, mock(TokenService.class), new UserMapper());
    var response =
        service.create(
            new RegisterRequest("A Citizen", "USER@example.tn", "DemoPassword123"),
            UserRole.CITIZEN);
    var captor = org.mockito.ArgumentCaptor.forClass(User.class);
    verify(repo).save(captor.capture());
    assertEquals("user@example.tn", response.email());
    assertTrue(encoder.matches("DemoPassword123", captor.getValue().passwordHash()));
    assertFalse(response.toString().contains("password"));
    assertEquals(UserRole.CITIZEN, response.role());
  }
}
