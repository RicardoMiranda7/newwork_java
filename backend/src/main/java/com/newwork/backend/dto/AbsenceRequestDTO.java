package com.newwork.backend.dto;

import com.newwork.backend.model.AbsenceStatus;
import java.time.LocalDate;
import lombok.Data;

@Data
public class AbsenceRequestDTO {

  private Long id;
  private UserSummaryDTO employee;
  private LocalDate startDate;
  private LocalDate endDate;
  private String reason;
  private AbsenceStatus status;
}

