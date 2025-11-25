package com.newwork.backend.security;

import com.newwork.backend.model.User;
import com.newwork.backend.model.UserProfile;
import com.newwork.backend.repository.ProfileRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component("profileSecurity")
@RequiredArgsConstructor
public class ProfileSecurity {

  private final ProfileRepository profileRepository;

  public boolean isOwnerOrManager(Long profileId, User currentUser) {
    return profileRepository.findById(profileId)
        .map(profile -> {

          boolean isOwner = profile.getUser().getId()
              .equals(currentUser.getId());

          boolean isManager = profile.getManager() != null &&
              profile.getManager().getId().equals(currentUser.getId());

          return isOwner || isManager;
        })
        .orElse(false); // If profile doesn't exist, deny access
  }

  public boolean isManager(Long profileId, User currentUser) {
    return profileRepository.findById(profileId)
        .map(profile -> profile.getManager() != null &&
            profile.getManager().getId().equals(currentUser.getId()))
        .orElse(false);
  }

  public boolean isManagerOfProfile(UserProfile profile, User currentUser) {
    return profile.getManager() != null &&
        profile.getManager().getId().equals(currentUser.getId());
  }

  public boolean isOwner(Long profileId, User currentUser) {
    return profileRepository.findById(profileId)
        .map(profile -> profile.getUser().getId().equals(currentUser.getId()))
        .orElse(false);
  }

  public boolean isOwnerOfProfile(UserProfile profile, User currentUser) {
    return profile.getUser() != null &&
        profile.getUser().getId().equals(currentUser.getId());
  }

  public UserProfile getProfileOfUser(User user) {
    return profileRepository.findByUserId(user.getId())
        .orElseThrow(() -> new EntityNotFoundException("Profile not found"));
  }
}