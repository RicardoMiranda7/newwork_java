package com.newwork.backend.repository;

import com.newwork.backend.model.AbsenceLedger;
import com.newwork.backend.model.AbsenceRequest;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AbsenceLedgerRepository extends JpaRepository<AbsenceLedger, Long> {
  List<AbsenceLedger> findByEmployeeId(Long employeeId);
  List<AbsenceLedger> findByAbsenceRequestId(Long absenceRequestId);
}