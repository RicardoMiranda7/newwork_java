package com.newwork.backend.service;

import com.newwork.backend.dto.AuthenticationResponse;
import com.newwork.backend.dto.LoginRequest;
import com.newwork.backend.repository.UserRepository;
import com.newwork.backend.security.JwtService;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final JwtService jwtService;
  private final AuthenticationManager authenticationManager;

  /**
   * Authenticates the user against stored credentials and issues a JWT.
   *
   * @param request The login request credentials
   * @return Basic auth confirmation response
   */
  @Transactional
  public AuthenticationResponse authenticate(LoginRequest request) {
    authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            request.getEmail(),
            request.getPassword())
    );

    var user = userRepository.findByEmail(request.getEmail())
        .orElseThrow();

    // user_id replicates Python behavior
    var jwtToken = jwtService.generateToken(Map.of("user_id", user.getId()),
        user);

    return AuthenticationResponse.builder()
        .access(jwtToken)
        .build();
  }
}