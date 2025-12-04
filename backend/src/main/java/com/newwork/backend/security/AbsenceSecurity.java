package com.newwork.backend.security;

import com.newwork.backend.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component("absenceSecurity")
@RequiredArgsConstructor
public class AbsenceSecurity {

  private final AbsenceRoleResolver roleResolver;

  public enum UpdateRole {
    MANAGER,
    OWNER
  }

  /**
   * Check if a user has permission to edit a certain absence request
   *
   * @Return True if the user has permission
   */
  @Transactional(readOnly = true)
  public boolean hasAbsenceUpdatePermission(Long absenceId, User currentUser) {
    try {
      roleResolver.resolveUpdateRole(absenceId, currentUser);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Check if the user is manager of the profile for a certain absence request
   */
  @Transactional(readOnly = true)
  public boolean isManager(Long absenceId, User currentUser) {
    return UpdateRole.MANAGER.name().equalsIgnoreCase(
        roleResolver.resolveUpdateRole(absenceId, currentUser));
  }

}