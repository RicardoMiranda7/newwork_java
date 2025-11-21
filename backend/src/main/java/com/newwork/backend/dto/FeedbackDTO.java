package com.newwork.backend.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class FeedbackDTO {

  private UserSummaryDTO author;
  private String text;
  private LocalDateTime createdAt;
}
