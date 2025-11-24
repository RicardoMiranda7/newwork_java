package com.newwork.backend.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.newwork.backend.util.CustomLocalDateTimeSerializer;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiErrorResponse {

  private String correlationId; // For tracing requests
  private int status;
  private String error;    // Short error name (e.g., "Bad Request")
  private String message;  // Detailed message
  private String path;     // The URL that failed
  @JsonSerialize(using = CustomLocalDateTimeSerializer.class)
  private LocalDateTime timestamp;
}