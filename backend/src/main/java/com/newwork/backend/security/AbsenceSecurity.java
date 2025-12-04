package com.newwork.backend.security;

import com.newwork.backend.model.AbsenceRequest;
import com.newwork.backend.model.User;
import com.newwork.backend.repository.AbsenceRequestRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("absenceSecurity")
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
  protected UpdateRole getUpdateRole(Long absenceId, User currentUser) {
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

  /**
   * Check if a user has permission to edit a certain absence request
   *
   * @Return True if the user has permission
   */
  @Transactional(readOnly = true)
  public boolean hasAbsenceUpdatePermission(Long absenceId, User currentUser) {
    try {
      getUpdateRole(absenceId, currentUser);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Expose if is manager role as flag. Can leverage Hibernate cache if
   * configured to avoid a second DB request, since one was done in
   * @hasUpdatePermission
   */
  @Transactional(readOnly = true)
  public boolean isManager(Long absenceId, User currentUser) {
    return getUpdateRole(absenceId, currentUser) == UpdateRole.MANAGER;
  }

}