package com.newwork.backend.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ProfileCoWorkerDTO {

  private Long id;
  private String fullName;
  private String jobTitle;
  private String bio;
  private LocalDateTime joinedAt;
  private String department;
//  private UserSummaryDTO manager; // Exclude manager for now
}