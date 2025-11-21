package com.newwork.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ProfileDTO {

  private Long id;
  private UserSummaryDTO user; // Nested DTO
  private String fullName;
  private String jobTitle;
  private BigDecimal salary; // Sensitive
  private String gender;     // Sensitive
  private LocalDate dateOfBirth; // Sensitive
  private String address;    // Sensitive
  private String phoneNumber; // Sensitive
  private String bio;
  private LocalDateTime joinedAt;
  private String department;
  private UserSummaryDTO manager;
}