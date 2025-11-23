package com.newwork.backend.repository;

import com.newwork.backend.model.AbsenceRequest;
import com.newwork.backend.model.AbsenceStatus;
import com.newwork.backend.model.User;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AbsenceRequestRepository extends
    JpaRepository<AbsenceRequest, Long> {

  List<AbsenceRequest> findByEmployee(User employee);

  @Query("""
      SELECT a FROM AbsenceRequest a
      WHERE a.employee = :employee
        AND a.status IN :statuses
        AND a.startDate <= :endDate
        AND a.endDate   >= :startDate
        AND (:excludeId IS NULL OR a.id <> :excludeId)
      """)
  List<AbsenceRequest> findOverlappingRequests(
      @Param("employee") User employee,
      @Param("statuses") List<AbsenceStatus> statuses,
      @Param("startDate") LocalDate startDate,
      @Param("endDate") LocalDate endDate,
      @Param("excludeId") Long excludeId
  );

  // Using JOIN FETCH to eagerly load associated Employee data, since LAZY was set
  @Query("""
      SELECT a FROM AbsenceRequest a
      JOIN FETCH a.employee
      WHERE (a.status = 'APPROVED'
             OR (a.employee.id = :employeeId AND a.status <> 'APPROVED'))
        AND (extract(year from a.startDate) = :year
             OR extract(year from a.endDate) = :year)
      """)
  List<AbsenceRequest> fillAllApprovedOrUserRequestsByYear(
      @Param("employeeId") long employeeId,
      @Param("year") int year
  );
}