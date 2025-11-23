package com.newwork.backend.controller;

import com.newwork.backend.dto.AbsenceBalanceResponse;
import com.newwork.backend.dto.AbsenceRequestDTO;
import com.newwork.backend.dto.AbsenceResponseDTO;
import com.newwork.backend.model.User;
import com.newwork.backend.service.AbsenceService;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

  @PostMapping
  public ResponseEntity<AbsenceRequestDTO> createRequest(
      @RequestBody AbsenceRequestDTO absenceRequestDto,
      @AuthenticationPrincipal User user) {

    // Inject employee from the JWT auth service (currently hardcoded in service layer)

    // Service handles the creation logic
    AbsenceRequestDTO savedRequestDTO = absenceService.handleNewAbsenceRequest(
        absenceRequestDto,
        user);

    return ResponseEntity.ok(savedRequestDTO);
  }

  @GetMapping
  public ResponseEntity<List<AbsenceResponseDTO>> getVisibleAbsenceForUser(
      @AuthenticationPrincipal User user) {
    // Service handles the creation logic
    var listOfAbsences = absenceService.handleListVisibleAbsencesForUser(
        user, 2025);

    return ResponseEntity.ok(listOfAbsences);
  }

  @PatchMapping(path = "/update-status")
  public ResponseEntity<AbsenceRequestDTO> updateRequestStatus(
      @RequestBody AbsenceRequestDTO absenceRequestDTO) {

    // Service handles the update logic
    var updatedRequestDTO = absenceService.handleAbsenceStatusChange(
        absenceRequestDTO);

    return ResponseEntity.ok(updatedRequestDTO);
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
