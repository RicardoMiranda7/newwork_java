package com.newwork.backend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
// Run this before Security and everything else
public class CorrelationIdFilter extends OncePerRequestFilter {

  private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
  private static final String CORRELATION_ID_LOG_VAR = "correlationId";

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    // 1. Get or Generate ID
    String correlationId = request.getHeader(CORRELATION_ID_HEADER);
    if (correlationId == null || correlationId.isBlank()) {
      correlationId = UUID.randomUUID().toString();
    }

    // 2. Add to MDC (for Logging)
    MDC.put(CORRELATION_ID_LOG_VAR, correlationId);

    // 3. Add to Request Attributes (so Controller/Advice can access it)
    request.setAttribute(CORRELATION_ID_LOG_VAR, correlationId);

    // 4. Add to Response Header (so Client sees it)
    response.addHeader(CORRELATION_ID_HEADER, correlationId);

    // 5. Log the request
    log.info("Incoming Request: {} {}", request.getMethod(),
        request.getRequestURI());

    try {
      filterChain.doFilter(request, response);
    } finally {
      // 6. Clean up MDC to prevent memory leaks or data bleeding into other requests
      MDC.remove(CORRELATION_ID_LOG_VAR);
    }
  }
}