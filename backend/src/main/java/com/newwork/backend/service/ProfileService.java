package com.newwork.backend.service;

import com.newwork.backend.mapper.ProfileMapper;
import com.newwork.backend.model.Profile;
import com.newwork.backend.model.User;
import com.newwork.backend.repository.ProfileRepository;
import com.newwork.backend.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

  private final ProfileRepository profileRepository;
  private final UserRepository userRepository;
  private final ProfileMapper profileMapper;


  @Transactional(readOnly = true)
  public Object getProfileData(Long profileId, User currentUser) {

    Profile profile = profileRepository.findById(profileId)
        .orElseThrow(() -> new EntityNotFoundException("Profile not found"));

    // Permission logic: Is Manager or Owner?
    boolean isOwner = profile.getUser().getId().equals(currentUser.getId());
    // Check if currentUser is the manager of this profile
    boolean isManager =
        profile.getManager() != null && profile.getManager().getId()
            .equals(currentUser.getId());

    if (isOwner || isManager) {
      return profileMapper.toDto(profile); // Return full DTO
    } else {
      return profileMapper.toCoWorkerDto(profile); // Return restricted DTO
    }
  }
}
