package com.newwork.backend.controller;

import com.newwork.backend.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
  public ResponseEntity<Object> getProfile(@PathVariable Long id) {

    Object profileData = profileService.getProfileData(id);

    return ResponseEntity.ok(profileData);
  }

}
