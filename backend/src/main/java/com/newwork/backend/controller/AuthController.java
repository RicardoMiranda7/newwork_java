package com.newwork.backend.controller;

import com.newwork.backend.dto.AuthenticationResponse;
import com.newwork.backend.dto.LoginRequest;
import com.newwork.backend.service.AuthService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/token")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Operations related to authentication")
public class AuthController {

  private final AuthService authService;

  @PostMapping("/")
  public ResponseEntity<AuthenticationResponse> authenticate(
      @RequestBody LoginRequest request) {
    return ResponseEntity.ok(authService.authenticate(request));
  }
}