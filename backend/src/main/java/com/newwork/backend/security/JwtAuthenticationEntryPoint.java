package com.newwork.backend.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.newwork.backend.dto.ApiErrorResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;


/**
 * Handles unauthorized access attempts by returning a structured JSON error
 * response. Since it happens before request reaches controller, the response it
 * built here directly.
 * <p>
 * Entry point runs before Spring MVC converts controller return values,
 * so it's necessary to serialize the error object manually and write raw JSON
 * into the servlet response.
 */
@Component
@Slf4j
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper;

  public JwtAuthenticationEntryPoint(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public void commence(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException authException)
      throws IOException, ServletException {
    // This is invoked when a user tries to access a secured endpoint without a token
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    // Get correlation ID set the Filter
    String correlationId = (String) request.getAttribute("correlationId");

    ApiErrorResponse errorResponse = ApiErrorResponse.builder()
        .timestamp(LocalDateTime.now())
        .status(HttpStatus.UNAUTHORIZED.value())
        .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
        .message("Authentication token is missing or invalid.")
        .path(request.getRequestURI())
        .correlationId(correlationId)
        .build();
    log.warn("JWT Error: Token is invalid or expired.");

    response.getOutputStream()
        .write(objectMapper.writeValueAsBytes(errorResponse));
    response.getOutputStream().flush();
  }
}