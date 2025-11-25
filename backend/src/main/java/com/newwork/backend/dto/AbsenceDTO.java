package com.newwork.backend.dto;

import com.newwork.backend.dto.validation.OnCreate;
import com.newwork.backend.dto.validation.OnUpdate;
import com.newwork.backend.model.AbsenceStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import java.time.LocalDate;
import lombok.Data;

@Data
public class AbsenceDTO {

  @Null(message = "ID must be null for new requests", groups = OnCreate.class)
  @NotNull(message = "ID is required for updates", groups = OnUpdate.class)
  private Long id;
  @NotNull(message = "Employee is required for updates", groups = OnUpdate.class)
  private String employee;
  @NotNull(message = "Start date is required")
  // Optional: Ensure start date is not in the past, decided not to enforce for flexibility
  // @FutureOrPresent(message = "Start date cannot be in the past")
  private LocalDate startDate;
  @NotNull(message = "End date is required")
  private LocalDate endDate;
  @NotNull(message = "Reason is required")
  private String reason;
  @NotNull(message = "Status is required for updates", groups = OnUpdate.class)
  private AbsenceStatus status;
}

