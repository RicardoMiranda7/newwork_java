package com.newwork.backend.exception;

import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

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

  // 400 Bad Request - Validation errors
  // Handle Validation Errors (e.g. missing fields)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, String>> handleValidationExceptions(
      MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getFieldErrors().forEach(error ->
        errors.put(error.getField(), error.getDefaultMessage()));
    return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
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

  // 404 Not Found - URL or Resource not found
  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<Map<String, String>> handleResourceNotFound(
      NoResourceFoundException ex) {
    return new ResponseEntity<>(Map.of("error", ex.getMessage()),
        HttpStatus.NOT_FOUND);
  }

  // 404 Not Found - User or Profile not found
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
