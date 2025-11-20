package com.newwork.backend.repository;

import com.newwork.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
  // Spring Data derives the SQL from the method name:
  // SELECT * FROM users WHERE email = ?
  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email);
}