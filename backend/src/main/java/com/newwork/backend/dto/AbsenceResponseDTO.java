package com.newwork.backend.dto;

import com.newwork.backend.model.AbsenceStatus;
import java.time.LocalDate;
import lombok.Data;

@Data
public class AbsenceResponseDTO {

  private Long id;
  private String employeeEmail;
  private LocalDate startDate;
  private LocalDate endDate;
  private String reason;
  private AbsenceStatus status;
}

