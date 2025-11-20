package com.newwork.backend.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<FeedbackRepository, Long> {
  List<FeedbackRepository> findByProfileId(Long profileId);
}