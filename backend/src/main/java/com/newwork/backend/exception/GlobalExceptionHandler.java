package com.newwork.backend.exception;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, String>> handleIllegalArgumentException(
      IllegalArgumentException ex) {
    return new ResponseEntity<>(Map.of("error", ex.getMessage()),
        HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<Map<String, String>> handleAccessDeniedException(
      AccessDeniedException ex) {
    return new ResponseEntity<>(Map.of("error", ex.getMessage()),
        HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<Map<String, String>> handleRuntimeException(
      RuntimeException ex) {

    // Log the exception details for debugging
    log.error(ex.getMessage(), ex);
    return new ResponseEntity<>(
        Map.of("error", "An unexpected error occurred."),
        HttpStatus.INTERNAL_SERVER_ERROR);
  }
}
