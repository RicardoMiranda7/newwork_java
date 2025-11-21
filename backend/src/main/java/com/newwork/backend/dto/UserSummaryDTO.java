package com.newwork.backend.dto;

import lombok.Data;

@Data
public class UserSummaryDTO {

  private Long id;
  private String email;
  private String username;
}