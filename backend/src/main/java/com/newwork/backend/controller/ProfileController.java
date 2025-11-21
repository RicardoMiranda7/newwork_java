package com.newwork.backend.controller;

import com.newwork.backend.model.User;
import com.newwork.backend.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
public class ProfileController {

  private final ProfileService profileService;

  @GetMapping("/{id}")
  public ResponseEntity<Object> getProfile(@PathVariable Long id,
      @AuthenticationPrincipal User user) {

    Object profileData = profileService.getProfileData(id, user);

    return ResponseEntity.ok(profileData);
  }

}
