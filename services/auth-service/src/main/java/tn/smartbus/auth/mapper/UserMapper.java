package tn.smartbus.auth.mapper;

import org.springframework.stereotype.Component;
import tn.smartbus.auth.dto.UserResponse;
import tn.smartbus.auth.entity.User;

@Component
public class UserMapper {
  public UserResponse toResponse(User u) {
    return new UserResponse(u.id(), u.fullName(), u.email(), u.role());
  }
}
