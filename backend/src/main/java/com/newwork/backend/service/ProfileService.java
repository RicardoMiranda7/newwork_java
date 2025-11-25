package com.newwork.backend.service;

import com.newwork.backend.dto.ProfileCoWorkerDTO;
import com.newwork.backend.dto.ProfileDTO;
import com.newwork.backend.mapper.ProfileMapper;
import com.newwork.backend.model.User;
import com.newwork.backend.model.UserProfile;
import com.newwork.backend.repository.ProfileRepository;
import com.newwork.backend.security.ProfileSecurity;
import jakarta.persistence.EntityNotFoundException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProfileService {

  private final ProfileRepository profileRepository;
  private final ProfileSecurity profileSecurity;
  private final ProfileMapper profileMapper;


  @Transactional(readOnly = true)
  public Object getProfileData(Long profileId, User currentUser) {
    UserProfile profile = profileRepository.findById(profileId)
        .orElseThrow(() -> new EntityNotFoundException("Profile not found"));

    if (profileSecurity.isOwnerOrManager(profileId, currentUser)) {
      return profileMapper.toDto(profile); // Return full DTO
    } else {
      return profileMapper.toCoWorkerDto(profile); // Return restricted DTO
    }
  }

  @Transactional(readOnly = true)
  public Page<ProfileCoWorkerDTO> getAllProfiles(Pageable pageable) {
    return profileRepository.findAll(pageable)
        .map(profileMapper::toCoWorkerDto);
  }

  @Transactional
  public ProfileDTO updateProfile(Long profileId, ProfileDTO profileDTO,
      User currentUser) {
    UserProfile profile = profileRepository.findById(profileId)
        .orElseThrow(() -> new EntityNotFoundException("Profile not found"));

    // Check if salary is being updated
    if (profileDTO.getSalary() != null
        && !Objects.equals(profileDTO.getSalary(), profile.getSalary())) {
      // If salary is changing, only the manager can do it.
      if (!profileSecurity.isManager(profileId, currentUser)) {
        throw new AccessDeniedException(
            "Only a manager can update the salary.");
      }
    }

    // Update entity from DTO and save
    profileMapper.updateEntityFromDto(profileDTO, profile);
    UserProfile updatedProfile = profileRepository.save(profile);

    return profileMapper.toDto(updatedProfile);
  }

  @Transactional(readOnly = true)
  public UserProfile getProfileOfUser(User user) {
    return profileRepository.findByUserId(user.getId())
        .orElseThrow(() -> new EntityNotFoundException("Profile not found"));
  }

}
