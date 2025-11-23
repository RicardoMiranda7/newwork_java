package com.newwork.backend.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AbsenceBalanceResponse {

  private int year;
  private int vacationDaysAllowance;
  private int vacationDaysBalance;
  private int vacationDaysBalanceNextYear;
}