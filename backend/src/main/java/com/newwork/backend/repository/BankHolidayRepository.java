package com.newwork.backend.repository;

import com.newwork.backend.model.AbsenceLedger;
import com.newwork.backend.model.BankHoliday;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BankHolidayRepository extends JpaRepository<BankHoliday, Long> {
  List<BankHoliday> findByYear(Integer year);
}