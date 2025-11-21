package com.newwork.backend.util;

import static java.util.stream.Collectors.toSet;

import com.newwork.backend.model.BankHoliday;
import java.time.LocalDate;
import java.util.Set;
import lombok.experimental.UtilityClass;

/**
 * Utility class for date-related operations.
 *
 * @UtilityClass Lombok annotation makes the class final, all methods static and
 * prevents instantiation.
 */
@UtilityClass
public class DateUtils {

  /**
   * Returns the maximum of two LocalDate values.
   */
  public LocalDate max(LocalDate a, LocalDate b) {
    return a.isAfter(b) ? a : b;
  }

  /**
   * Returns the minimum of two LocalDate values.
   */
  public LocalDate min(LocalDate a, LocalDate b) {
    return a.isBefore(b) ? a : b;
  }

  /**
   * Transforms a set of BankHoliday objects to a set of LocalDate objects.
   *
   * @param dates Set of BankHoliday objects
   * @return Set of LocalDate objects
   */
  public Set<LocalDate> transformBankHolidaysToDateSet(Set<BankHoliday> dates) {
    return dates.stream().map(BankHoliday::getDate).collect(toSet());
  }
}
