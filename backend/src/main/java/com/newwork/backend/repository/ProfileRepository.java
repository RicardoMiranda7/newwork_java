package com.newwork.backend.repository;

import com.newwork.backend.model.UserProfile;
import jakarta.annotation.Nonnull;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProfileRepository extends JpaRepository<UserProfile, Long> {

  Optional<UserProfile> findByUserId(Long userId);

  @Override
  @Nonnull
  // Tell JPA to eager-fetch these relationships in one go, fixing N+1 problem
  // This is one possible way to fix
  @EntityGraph(attributePaths = {"user", "manager"})
  Page<UserProfile> findAll(@Nonnull Pageable pageable);
}