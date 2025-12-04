package com.newwork.backend.security;

import com.newwork.backend.model.AbsenceRequest;
import com.newwork.backend.model.User;
import com.newwork.backend.repository.AbsenceRequestRepository;
import com.newwork.backend.repository.ProfileRepository;
import com.newwork.backend.security.AbsenceSecurity.UpdateRole;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Resolver approach to deal with Spring's Proxy self-invocation problems
 */
@Component
@RequiredArgsConstructor
public class AbsenceRoleResolver {

  private final AbsenceRequestRepository absenceRequestRepository;
  private final ProfileRepository profileRepository;

  /**
   * Checks permissions and returns the Role of the user for this specific
   * request.
   * <p/>
   * Determines if the current user is MANAGER or OWNER of the absence request
   * they are trying to update.
   * </p>
   * Results are cached in Redis to prevent querying the DB again. Key format:
   * "absence_roles::123-456" (AbsenceID-UserID)
   *
   * @return MANAGER or OWNER
   * @throws AccessDeniedException if the user has no rights.
   */
  @Transactional(readOnly = true)
  @Cacheable(value = "absence_roles", key = "#absenceId + '-' + #currentUser.id")
  public String resolveUpdateRole(Long absenceId, User currentUser) {

    AbsenceRequest request = absenceRequestRepository.findById(absenceId)
        .orElseThrow(
            () -> new IllegalArgumentException("Absence request not found"));

    var profileOfAbsence = profileRepository.findByUserId(
            request.getEmployee().getId())
        .orElseThrow(() -> new RuntimeException("Profile not found"));

    // 1. Check if Manager
    if (profileOfAbsence.getManager() != null &&
        profileOfAbsence.getManager().getId().equals(currentUser.getId())) {
      return UpdateRole.MANAGER.name();
    }

    // 2. Check if Owner
    if (request.getEmployee().getId().equals(currentUser.getId())) {
      return UpdateRole.OWNER.name();
    }

    throw new AccessDeniedException(
        "You do not have permission to update this request.");
  }
}