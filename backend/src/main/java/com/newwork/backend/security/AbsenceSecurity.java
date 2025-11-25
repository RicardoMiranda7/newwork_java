package com.newwork.backend.security;

import com.newwork.backend.model.AbsenceRequest;
import com.newwork.backend.model.User;
import com.newwork.backend.repository.AbsenceRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class AbsenceSecurity {

  private final AbsenceRequestRepository absenceRequestRepository;
  private final ProfileSecurity profileSecurity;

  public enum UpdateRole {
    MANAGER,
    OWNER
  }

  /**
   * Checks permissions and returns the Role of the user for this specific
   * request.
   * <p/>
   * Determines if the current user is MANAGER or OWNER of the absence request
   * they are trying to update.
   *
   * @throws AccessDeniedException if the user has no rights.
   * @return MANAGER or OWNER
   */
  @Transactional(readOnly = true)
  public UpdateRole getUpdateRole(Long absenceId, User currentUser) {
    AbsenceRequest request = absenceRequestRepository.findById(absenceId)
        .orElseThrow(
            () -> new IllegalArgumentException("Absence request not found"));

    // Fetch the user profile for this absence
    var profileOfAbsence = profileSecurity.getProfileOfUser(
        request.getEmployee());

    // 1. Check if Manager
    if (profileSecurity.isManagerOfProfile(profileOfAbsence, currentUser)) {
      return UpdateRole.MANAGER;
    }

    // 2. Check if Owner
    if (profileSecurity.isOwnerOfProfile(profileOfAbsence, currentUser)) {
      return UpdateRole.OWNER;
    }

    // 3. Neither
    throw new AccessDeniedException(
        "You do not have permission to update this request.");
  }
}