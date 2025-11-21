package com.newwork.backend.service;

import com.newwork.backend.dto.AbsenceRequestDTO;
import com.newwork.backend.mapper.AbsenceRequestMapper;
import com.newwork.backend.model.AbsenceLedger;
import com.newwork.backend.model.AbsenceRequest;
import com.newwork.backend.model.AbsenceStatus;
import com.newwork.backend.model.User;
import com.newwork.backend.repository.AbsenceLedgerRepository;
import com.newwork.backend.repository.AbsenceRequestRepository;
import com.newwork.backend.repository.BankHolidayRepository;
import com.newwork.backend.repository.UserRepository;
import com.newwork.backend.util.DateUtils;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AbsenceService {

  private final AbsenceLedgerRepository ledgerRepository;
  private final AbsenceRequestRepository requestRepository;
  private final UserRepository userRepository;
  private final BankHolidayRepository bankHolidayRepository;

  private final AbsenceRequestMapper requestMapper;

  private static final int YEARLY_VACATION_ALLOWANCE = 25;

  /**
   * Calculates the vacation balance for a profile and year by summing ledger
   * entries.
   */
  public int getVacationBalance(User user, int year) {

    // Ensure yearly allowance entry exists
    boolean allowanceExists = ledgerRepository.existsByEmployeeAndYearAndDescription(
        user, year, AbsenceLedgerRepository.YEARLY_ALLOWANCE_DESCRIPTION);
    if (!allowanceExists) {
      recordTransaction(user, null, year, YEARLY_VACATION_ALLOWANCE,
          AbsenceLedgerRepository.YEARLY_ALLOWANCE_DESCRIPTION);
    }

    Integer balance = ledgerRepository.sumAmountByEmployeeAndYear(user, year);

    return balance != null ? balance : 0;
  }


  public List<AbsenceRequest> getAbsenceRequestsForUser(User user) {
    return requestRepository.findByEmployee(user);
  }

  /**
   * Records a transaction in the absence ledger.
   *
   * @param user        The employee
   * @param request     The associated absence request (can be null)
   * @param year        The year of the transaction
   * @param amount      The amount (positive for credit, negative for debit)
   * @param description Description of the transaction
   */
  private void recordTransaction(User user, AbsenceRequest request, int year,
      int amount, String description) {
    // Implementation to record a transaction in the absence ledger using lombok builder
    AbsenceLedger ledgerEntry = AbsenceLedger.builder()
        .employee(user)
        .absenceRequest(request).
        year(year)
        .amount(amount)
        .description(description)
        .build();
    ledgerRepository.save(ledgerEntry);
  }


  /**
   * Handles a new absence request by saving it and then validating and debiting
   * the absence. Whole method is transactional to ensure atomicity, preventing
   * a new absence request being saved even if validation fails.
   *
   * @param absenceRequestDTO The new absence request DTO
   */
  @Transactional
  public AbsenceRequestDTO handleNewAbsenceRequest(
      AbsenceRequestDTO absenceRequestDTO) {

    // Map DTO to Entity
    AbsenceRequest request = requestMapper.toEntity(absenceRequestDTO);

    // Temp: Inject employee - to be replaced with JWT auth service
    User currentUser = userRepository.findByEmail("johnsmith@example.com")
        .orElseThrow(() -> new RuntimeException("User not found"));
    request.setEmployee(currentUser);

    // First, save the absence request to have an ID.
    var newAbsence = requestRepository.save(request);

    // Then run the validation and create the initial debit.
    validateAndDebitAbsenceRequest(newAbsence);

    return requestMapper.toDto(newAbsence);
  }


  /**
   * Validates an absence request against the employee's vacation balance and
   * other vacation periods. If valid, records debit transactions in the absence
   * ledger.
   *
   * @param request The absence request to validate and debit
   * @throws IllegalArgumentException if validation fails
   */
  private void validateAndDebitAbsenceRequest(AbsenceRequest request) {
    LocalDate startDate = request.getStartDate();
    LocalDate endDate = request.getEndDate();

    // Basic date validation
    if (startDate.isAfter(endDate)) {
      throw new IllegalArgumentException("Start date must be before end date");
    }

    // Check for intersecting absence requests (exclude itself).
    // Throw exception if any exist.
    var intersection = requestRepository.findOverlappingRequests(
        request.getEmployee(),
        List.of(AbsenceStatus.PENDING, AbsenceStatus.APPROVED),
        startDate,
        endDate,
        request.getId());
    if (!intersection.isEmpty()) {
      throw new IllegalArgumentException(
          "The requested absence period overlaps with an existing "
              + "absence request.");
    }

    //  When multi-year absences are requested, split the validation and debit
    //  across the years.
    for (int year = startDate.getYear(); year <= endDate.getYear(); year++) {
      // Fetch current vacation balance and raise error if zero or negative.
      int currentBalance = getVacationBalance(request.getEmployee(), year);
      if (currentBalance <= 0) {
        throw new IllegalArgumentException(
            "Insufficient vacation balance for year " + year);
      }

      // Determine the start and end dates for this year segment
      LocalDate segmentStart = DateUtils.max(startDate,
          LocalDate.of(year, 1, 1));
      LocalDate segmentEnd = DateUtils.min(endDate,
          LocalDate.of(year, 12, 31));

      // Fetch bank holidays for the year.
      var holidays = bankHolidayRepository.findByDateBetween(segmentStart,
          segmentEnd);

      // Calculate business days in this segment
      int requestedDays = calculateBusinessDaysForPeriod(segmentStart,
          segmentEnd, DateUtils.transformBankHolidaysToDateSet(holidays));

      if (requestedDays <= 0) {
        throw new IllegalArgumentException(
            "No business days requested in year " + year);
      }
      if (requestedDays > currentBalance) {
        throw new IllegalArgumentException(
            """
                 Insufficient vacation balance.
                 You have %s days remaining for year %d,\s
                 but this request is for %d business days.
                """.formatted(currentBalance, year, requestedDays));
      }

      // If all checks pass, record the debit transaction
      recordTransaction(request.getEmployee(), request, year, -requestedDays,
          """
              Absence request submitted ( %s to %s)
              """.formatted(startDate, endDate));
    }

  }


  /**
   * Calculates business days between two dates Replaces numpy.busday_count from
   * Python implementation.
   *
   * @param startDate    Start date
   * @param endDate      End date
   * @param bankHolidays Set of bank holiday dates
   * @return Number of business days
   */
  public int calculateBusinessDaysForPeriod(LocalDate startDate,
      LocalDate endDate, Set<LocalDate> bankHolidays) {
    return startDate.datesUntil(endDate.plusDays(1))
        .filter(date -> {
          // Exclude weekends
          boolean isWeekend =
              date.getDayOfWeek().getValue() >= DayOfWeek.SATURDAY.getValue();
          // Exclude bank holidays
          boolean isBankHoliday = bankHolidays.contains(date);
          return !isWeekend && !isBankHoliday;
        })
        .toArray().length;
  }
}
