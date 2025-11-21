package com.newwork.backend.controller;

import com.newwork.backend.dto.AuthenticationResponse;
import com.newwork.backend.dto.LoginRequest;
import com.newwork.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/token")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/")
  public ResponseEntity<AuthenticationResponse> authenticate(
      @RequestBody LoginRequest request) {
    return ResponseEntity.ok(authService.authenticate(request));
  }
}