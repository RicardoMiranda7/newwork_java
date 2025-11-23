package com.newwork.backend.controller;

import com.newwork.backend.dto.ProfileCoWorkerDTO;
import com.newwork.backend.dto.ProfileDTO;
import com.newwork.backend.model.User;
import com.newwork.backend.service.ProfileService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/profiles")
@RequiredArgsConstructor
@Tag(name = "Profiles", description = "Operations related to employee profiles")
public class ProfileController {

  private final ProfileService profileService;

  @GetMapping("/{id}")
  public ResponseEntity<Object> getProfile(@PathVariable Long id,
      @AuthenticationPrincipal User user) {

    Object profileData = profileService.getProfileData(id, user);

    return ResponseEntity.ok(profileData);
  }

  /**
   * Endpoint to list all co-worker profiles with restricted information.
   *
   * @return List of ProfileCoWorkerDTOs
   */
  @GetMapping("/")
  public ResponseEntity<Page<ProfileCoWorkerDTO>> getAllProfiles(
      @PageableDefault(size = 20, sort = "fullName") Pageable pageable
  ) {
    return ResponseEntity.ok(profileService.getAllProfiles(pageable));
  }

  /**
   * Endpoint to update a profile. Only the profile owner or their manager can
   * perform this operation.
   *
   * @param id          ID of the profile to update
   * @param profileDto  Profile data to update
   * @param currentUser Currently authenticated user
   * @return Updated ProfileDTO
   */
  @PutMapping("/{id}")
  @PreAuthorize("@profileSecurity.isOwnerOrManager(#id, #currentUser)")
  public ResponseEntity<ProfileDTO> updateProfile(
      @PathVariable Long id,
      @RequestBody ProfileDTO profileDto,
      @AuthenticationPrincipal User currentUser
  ) {
    return ResponseEntity.ok(
        profileService.updateProfile(id, profileDto, currentUser));
  }

}
