package com.newwork.backend.exception;

import static com.newwork.backend.util.ApiErrorUtils.buildResponse;

import com.newwork.backend.dto.ApiErrorResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Global exception handler to catch and process exceptions thrown by
 * controllers. Translates exceptions into standardized JSON error responses.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

  // 400 Bad Request - Logical errors
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiErrorResponse> handleIllegalArgumentException(
      IllegalArgumentException ex,
      HttpServletRequest request) {

    log.warn("Bad Request: {}", ex.getMessage());
    return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request,
        ex.getMessage());
  }

  // 400 Bad Request - Validation errors
  // Handle Validation Errors (e.g. missing fields)
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiErrorResponse> handleValidationExceptions(
      MethodArgumentNotValidException ex,
      HttpServletRequest request) {
    // Combine all validation errors into one string
    String errorMessage = ex.getBindingResult().getFieldErrors().stream()
        .map(error -> error.getField() + ": " + error.getDefaultMessage())
        .collect(Collectors.joining(", "));

    log.warn("Bad Request: {}", ex.getMessage());
    return buildResponse(HttpStatus.BAD_REQUEST, errorMessage, request);
  }


  // 401 Unauthorized - Login failed
  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ApiErrorResponse> handleBadCredentialsException(
      BadCredentialsException ex,
      HttpServletRequest request) {

    log.warn("Authentication Failed: {}", ex.getMessage());
    return buildResponse(HttpStatus.UNAUTHORIZED, "Invalid email or password",
        request);
  }

  // Handle Expired JWTs
  // Aggregate JWT related exceptions
  @ExceptionHandler({ExpiredJwtException.class, SignatureException.class,
      MalformedJwtException.class})
  public ResponseEntity<ApiErrorResponse> handleJwtExceptions(Exception ex,
      HttpServletRequest request) {

    log.warn("JWT Error: {}", ex.getMessage());
    return buildResponse(HttpStatus.UNAUTHORIZED,
        "Token is invalid or expired.", request);
  }

  // 403 Forbidden - Authenticated but not allowed (e.g. Employee trying to edit salary)
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ApiErrorResponse> handleAccessDeniedException(
      AccessDeniedException ex,
      HttpServletRequest request) {

    log.warn("Access Denied: {}", ex.getMessage());
    return buildResponse(HttpStatus.FORBIDDEN,
        "You do not have permission to perform this action.", request,
        ex.getMessage());
  }

  // 404 Not Found - URL or Resource not found
  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ApiErrorResponse> handleResourceNotFound(
      NoResourceFoundException ex,
      HttpServletRequest request) {

    log.warn("Resource Not Found: {}", ex.getMessage());
    return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
  }

  // 404 Not Found - User or Profile not found
  @ExceptionHandler(UsernameNotFoundException.class)
  public ResponseEntity<ApiErrorResponse> handleUserNotFound(
      UsernameNotFoundException ex,
      HttpServletRequest request) {

    log.warn("User Not Found: {}", ex.getMessage());
    return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
  }

  // 405 Method Not Allowed - Client requests method not allowed
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ApiErrorResponse> handleHttpMethodNotSupported(
      Exception ex,
      HttpServletRequest request) {

    log.warn("Client requested method not allowed: ", ex);
    return buildResponse(HttpStatus.METHOD_NOT_ALLOWED,
        ex.getMessage(), request);
  }

  // 500 Internal Server Error - Catch-all
  // Catch Exception instead of RuntimeException to be safer
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiErrorResponse> handleGeneralException(
      Exception ex,
      HttpServletRequest request) {

    log.error("Unexpected error: ", ex);
    return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
        "An unexpected error occurred.", request);
  }
}
