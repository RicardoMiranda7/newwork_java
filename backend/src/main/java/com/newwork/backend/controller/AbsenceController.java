package com.newwork.backend.controller;

import com.newwork.backend.dto.AbsenceRequestDTO;
import com.newwork.backend.model.User;
import com.newwork.backend.service.AbsenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/absences")
@RequiredArgsConstructor
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

  @PatchMapping(path = "/update-status")
  public ResponseEntity<AbsenceRequestDTO> updateRequestStatus(
      @RequestBody AbsenceRequestDTO absenceRequestDTO) {

    // Service handles the update logic
    AbsenceRequestDTO updatedRequestDTO = absenceService.handleAbsenceStatusChange(
        absenceRequestDTO);

    return ResponseEntity.ok(updatedRequestDTO);
  }

}
