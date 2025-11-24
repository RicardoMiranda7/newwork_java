package com.newwork.backend.config;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.newwork.backend.dto.ApiErrorResponse;
import com.newwork.backend.dto.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalResponseAdvice implements ResponseBodyAdvice<Object> {

  private final ObjectMapper objectMapper;

  @Override
  public boolean supports(@NonNull MethodParameter returnType,
      @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
    // Apply to everything
    return true;
  }

  @Override
  public Object beforeBodyWrite(Object body,
      @NonNull MethodParameter returnType,
      @NonNull MediaType selectedContentType,
      @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
      ServerHttpRequest request,
      @NonNull ServerHttpResponse response) {

    // 1. Exclude Swagger/OpenAPI endpoints (don't want to wrap the API documentation JSON)
    String path = request.getURI().getPath();
    if (path.contains("/v3/api-docs") || path.contains("/swagger-ui")) {
      return body;
    }

    // 2. If it's already an Error Response, don't wrap it again (it has its own structure)
    if (body instanceof ApiErrorResponse) {
      return body;
    }

    // 3. If it's already wrapped (e.g. manual wrapping), skip
    if (body instanceof ApiResponse) {
      return body;
    }

    // 4. Get Correlation ID from the Request Attributes (set by your Filter)
    // Need to cast ServerHttpRequest to ServletServerHttpRequest to access attributes
    String correlationId = "";
    if (request instanceof ServletServerHttpRequest servletRequest) {
      HttpServletRequest httpServletRequest = servletRequest.getServletRequest();
      correlationId = (String) httpServletRequest.getAttribute("correlationId");
    }

    // 5. Build the Wrapper
    ApiResponse<Object> wrappedResponse = ApiResponse.builder()
        .correlationId(correlationId)
        .timestamp(LocalDateTime.now())
        .data(body)
        .build();

    // 6. Handle String return types (Edge Case)
    // Spring treats Strings differently. If the controller returns a String,
    // Spring tries to use a StringHttpMessageConverter which cannot handle the Object wrapper.
    // Must manually serialize the wrapper to JSON string.
    if (body instanceof String) {
      try {
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return objectMapper.writeValueAsString(wrappedResponse);
      } catch (JsonProcessingException e) {
        throw new RuntimeException("Error wrapping String response", e);
      }
    }

    return wrappedResponse;
  }
}