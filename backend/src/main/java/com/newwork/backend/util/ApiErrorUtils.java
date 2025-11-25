package com.newwork.backend.util;

import com.newwork.backend.dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ApiErrorUtils {

  // Helper method to build the standard response
  public static ResponseEntity<ApiErrorResponse> buildResponse(
      HttpStatus status,
      String message, HttpServletRequest request) {
    return buildResponse(status, message, request, "");
  }

  // Helper method to build the standard response
  public static ResponseEntity<ApiErrorResponse> buildResponse(
      HttpStatus status,
      String message, HttpServletRequest request, String details) {
    String correlationId = (String) request.getAttribute("correlationId");

    ApiErrorResponse response = ApiErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .status(status.value())
        .error(status.getReasonPhrase())
        .message(message)
        .details(details)
        .path(request.getRequestURI())
        .correlationId(correlationId)
        .build();

    return new ResponseEntity<>(response, status);
  }
}
