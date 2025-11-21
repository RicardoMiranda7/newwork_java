package com.newwork.backend.repository;

import com.newwork.backend.model.BankHoliday;
import java.time.LocalDate;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankHolidayRepository extends
    JpaRepository<BankHoliday, Long> {

  boolean existsByDate(LocalDate date);
  Set<BankHoliday> findByDateBetween(LocalDate dateAfter, LocalDate dateBefore);
}