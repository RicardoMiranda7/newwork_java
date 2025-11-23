package com.newwork.backend.dto;

import com.newwork.backend.model.AbsenceStatus;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;

@Data
public class AbsenceDTO {

  private Long id;
  private String employee;
  @NotNull(message = "Start date is required")
  // Optional: Ensure start date is not in the past, decided not to enforce for flexibility
  // @FutureOrPresent(message = "Start date cannot be in the past")
  private LocalDate startDate;
  @NotNull(message = "End date is required")
  private LocalDate endDate;
  @NotNull(message = "Reason is required")
  private String reason;
  private AbsenceStatus status;
}

