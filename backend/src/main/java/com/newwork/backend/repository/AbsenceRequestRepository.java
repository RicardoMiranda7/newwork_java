package com.newwork.backend.repository;

import com.newwork.backend.model.AbsenceRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AbsenceRequestRepository extends JpaRepository<AbsenceRequest, Long> {
  List<AbsenceRequest> findByEmployeeId(Long employeeId);

  // For the overlap check logic later:
  // find requests for this employee that overlap with start/end dates
}