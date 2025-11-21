package com.newwork.backend.repository;

import com.newwork.backend.model.AbsenceLedger;
import com.newwork.backend.model.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AbsenceLedgerRepository extends JpaRepository<AbsenceLedger, Long> {

  final static String YEARLY_ALLOWANCE_DESCRIPTION = "Yearly Allowance";

  List<AbsenceLedger> findByEmployeeId(Long employeeId);
  List<AbsenceLedger> findByAbsenceRequestId(Long absenceRequestId);

  @Query("SELECT SUM(a.amount) FROM AbsenceLedger a WHERE a.employee = :employee AND a.year = :year")
  Integer sumAmountByEmployeeAndYear(@Param("employee") User employee,
      @Param("year") int year);

  boolean existsByEmployeeAndYearAndDescription(User employee, int year,
      String description);
}