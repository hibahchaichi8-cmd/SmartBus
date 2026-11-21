package tn.smartbus.auth.repository;

import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;
import tn.smartbus.auth.entity.User;

public interface UserRepository extends MongoRepository<User, String> {
  Optional<User> findByEmail(String email);
}
