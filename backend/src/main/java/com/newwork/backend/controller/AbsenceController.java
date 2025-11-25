package com.newwork.backend.controller;

import com.newwork.backend.dto.AbsenceBalanceResponse;
import com.newwork.backend.dto.AbsenceDTO;
import com.newwork.backend.dto.validation.OnCreate;
import com.newwork.backend.dto.validation.OnUpdate;
import com.newwork.backend.model.User;
import com.newwork.backend.security.AbsenceSecurity;
import com.newwork.backend.security.AbsenceSecurity.UpdateRole;
import com.newwork.backend.service.AbsenceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.groups.Default;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/absences")
@RequiredArgsConstructor
@Tag(name = "Absences", description = "Operations related to employees absences")
public class AbsenceController {

  private final AbsenceService absenceService;
  private final AbsenceSecurity absenceSecurity;

  @PostMapping("/")
  public ResponseEntity<AbsenceDTO> createRequest(
      @Validated({
          OnCreate.class,
          Default.class}) @RequestBody AbsenceDTO absenceRequestDto,
      @AuthenticationPrincipal User user) {

    // Service handles the creation logic
    AbsenceDTO savedRequestDTO = absenceService.handleNewAbsenceRequest(
        absenceRequestDto,
        user);

    return ResponseEntity.ok(savedRequestDTO);
  }

  @GetMapping("/")
  public ResponseEntity<List<AbsenceDTO>> getVisibleAbsencesForUser(
      @RequestParam(required = false) Integer year,
      @AuthenticationPrincipal User user) {
    // User current year if not provided
    int absenceYear = (year != null) ? year : java.time.Year.now().getValue();

    var listOfAbsences = absenceService.handleListVisibleAbsencesForUser(
        user, absenceYear);

    return ResponseEntity.ok(listOfAbsences);
  }

  @PatchMapping(path = "/update")
  public ResponseEntity<AbsenceDTO> updateRequestStatus(
      @Validated({
          OnUpdate.class,
          Default.class}) @RequestBody AbsenceDTO absenceRequestDto,
      @AuthenticationPrincipal User user) {

    // Custom replacement for PreAuthorize, since its necessary to pass the role
    var role = absenceSecurity.getUpdateRole(absenceRequestDto.getId(), user);

    // Check if is Manager
    var isManager = role.equals(UpdateRole.MANAGER);

    var result = absenceService.handleAbsenceUpdate(absenceRequestDto,
        isManager);

    return ResponseEntity.ok(result);
  }

  @GetMapping("/absence-balance")
  @PreAuthorize("@profileSecurity.isOwnerOrManager(#id, #user)")
  public ResponseEntity<AbsenceBalanceResponse> getAbsenceBalance(
      @RequestParam Long id,
      @RequestParam(required = false) Integer year,
      @AuthenticationPrincipal User user) {

    int absenceYear = (year != null) ? year : java.time.Year.now().getValue();

    var balanceResponse = absenceService.handleAbsenceBalanceRequest(
        id, absenceYear);

    return ResponseEntity.ok(balanceResponse);
  }

}
