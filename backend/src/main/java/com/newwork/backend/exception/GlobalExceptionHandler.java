package com.newwork.backend.exception;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  // 400 Bad Request - Logical errors
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Map<String, String>> handleIllegalArgumentException(
      IllegalArgumentException ex) {
    return new ResponseEntity<>(Map.of("error", ex.getMessage()),
        HttpStatus.BAD_REQUEST);
  }

  // 401 Unauthorized - Login failed
  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<Map<String, String>> handleBadCredentialsException(
      BadCredentialsException ex) {
    return new ResponseEntity<>(Map.of("error", "Invalid email or password"),
        HttpStatus.UNAUTHORIZED);
  }

  // 403 Forbidden - Authenticated but not allowed (e.g. Employee trying to edit salary)
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<Map<String, String>> handleAccessDeniedException(
      AccessDeniedException ex) {
    return new ResponseEntity<>(
        Map.of("error", "You do not have permission to perform this action."),
        HttpStatus.FORBIDDEN);
  }


  // 404 Not Found - User or Profile not found
  // (Assuming you throw RuntimeException("User not found") currently,
  // but catching specific exceptions is better if you create custom ones)
  @ExceptionHandler(UsernameNotFoundException.class)
  public ResponseEntity<Map<String, String>> handleUserNotFound(
      UsernameNotFoundException ex) {
    return new ResponseEntity<>(Map.of("error", ex.getMessage()),
        HttpStatus.NOT_FOUND);
  }

  // 500 Internal Server Error - Catch-all
  @ExceptionHandler(Exception.class)
  // Catch Exception instead of RuntimeException to be safer
  public ResponseEntity<Map<String, String>> handleGeneralException(
      Exception ex) {
    log.error("Unexpected error: ", ex);
    return new ResponseEntity<>(
        Map.of("error", "An unexpected error occurred."),
        HttpStatus.INTERNAL_SERVER_ERROR
    );
  }
}
