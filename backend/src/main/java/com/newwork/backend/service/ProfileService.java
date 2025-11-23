package com.newwork.backend.service;

import com.newwork.backend.dto.ProfileCoWorkerDTO;
import com.newwork.backend.dto.ProfileDTO;
import com.newwork.backend.mapper.ProfileMapper;
import com.newwork.backend.model.User;
import com.newwork.backend.model.UserProfile;
import com.newwork.backend.repository.ProfileRepository;
import com.newwork.backend.security.ProfileSecurity;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
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
  public List<ProfileCoWorkerDTO> getAllCoWorkerProfiles() {
    List<UserProfile> profiles = profileRepository.findAll();
    return profiles.stream()
        .map(profileMapper::toCoWorkerDto)
        .collect(Collectors.toList());
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

}
