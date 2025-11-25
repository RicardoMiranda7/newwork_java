package com.newwork.backend.service;

import com.newwork.backend.dto.AbsenceBalanceResponse;
import com.newwork.backend.dto.AbsenceDTO;
import com.newwork.backend.mapper.AbsenceMapper;
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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AbsenceService {

  private final AbsenceLedgerRepository ledgerRepository;
  private final AbsenceRequestRepository requestRepository;
  private final UserRepository userRepository;
  private final BankHolidayRepository bankHolidayRepository;

  private final AbsenceMapper requestMapper;

  private static final int YEARLY_VACATION_ALLOWANCE = 25;

  /**
   * Calculates the vacation balance for a profile and year by summing ledger
   * entries.
   */
  private int getVacationBalance(User user, int year) {

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
   * @param absenceDto The new absence request DTO
   * @param user       The employee making the request
   */
  @Transactional
  public AbsenceDTO handleNewAbsenceRequest(
      AbsenceDTO absenceDto,
      User user) {

    // Map DTO to Entity
    AbsenceRequest request = requestMapper.toEntity(absenceDto);

    // Set the current user
    request.setEmployee(user);

    // First, save the absence request to have an ID.
    var newAbsence = requestRepository.save(request);

    // Then run the validation and create the initial debit.
    validateAndDebitAbsenceRequest(newAbsence);

    return requestMapper.toDto(newAbsence);
  }


  /**
   * Handles all possible changes performed to an AbsenceRequest. Uses role
   * based permissions to ensure the correct modification are executed.
   *
   * @param absenceDto The object containing the changes requested.
   * @param isManager  Flag to determine if the current user is manager or not.
   * @return The new, up-to-date, DTO
   */
  @Transactional
  public AbsenceDTO handleAbsenceUpdate(
      AbsenceDTO absenceDto,
      boolean isManager) {
    // Map DTO to Entity
    var clientRequest = requestMapper.toEntity(absenceDto);

    // Check if request exists
    var existingRequest = requestRepository.findById(clientRequest.getId())
        .orElseThrow(() -> new IllegalArgumentException(
            "Absence request not found"));

    // If clients sends the exact same request
    if (existingRequest.equals(clientRequest)) {
      throw new IllegalArgumentException("No changes detected.");
    }

    // Rejected status is final
    if (existingRequest.getStatus() == AbsenceStatus.REJECTED) {
      throw new IllegalArgumentException(
          "This request is already rejected. Please create a new Absence Request.");
    }

    // Extract and validate new status
    AbsenceStatus newStatus = clientRequest.getStatus();
    // Cannot change back to pending
    if (AbsenceStatus.PENDING.equals(newStatus)
        && !existingRequest.getStatus().equals(AbsenceStatus.PENDING)) {
      throw new IllegalArgumentException("New status is invalid");
    }

    AbsenceDTO replyDto;
    if (isManager) {
      replyDto = handleManagerUpdate(existingRequest, clientRequest, newStatus);
    } else {
      replyDto = handleOwnerUpdate(existingRequest, clientRequest, newStatus);
    }

    return replyDto;
  }


  /**
   * Handles status changes for an absence request, adjusting ledger entries as
   * needed. It persists both ledger and status changes
   *
   * @Return New, up-to-date, DTO
   */
  private AbsenceDTO handleAbsenceStatusChange(
      AbsenceRequest existingRequest,
      AbsenceStatus newStatus) {
    // If rejecting or cancelling, credit the debited days back
    if (newStatus == AbsenceStatus.REJECTED) {
      revertLedgerForRequest(existingRequest);
    }

    // Save the new status
    existingRequest.setStatus(newStatus);
    requestRepository.save(existingRequest);

    // Return the DTO
    return requestMapper.toDto(existingRequest);
  }

  /**
   * Calculates and reverts the current absence ledger balance per year for a
   * given AbsenceRequest
   *
   * @param existingRequest The AbsenceRequest to revert
   */
  private void revertLedgerForRequest(AbsenceRequest existingRequest) {
    //  When multi-year absences are requested, split the validation and debit
    //  across the years.
    for (int year = existingRequest.getStartDate().getYear();
        year <= existingRequest.getEndDate().getYear(); year++) {

      // The outstanding (result of sum all debits and credits for
      // a request for a year) should be the amount to be reverted
      var currentBalance = ledgerRepository.sumAmountByAbsenceRequestAndYear(
          existingRequest, year);
      if (currentBalance != 0) {
        recordTransaction(existingRequest.getEmployee(),
            existingRequest,
            year,
            -currentBalance,
            "Absence request rejected or modified");
      }
    }
  }

  /**
   * Handles absence balance requests by calculating the current and next year
   * balances. It inserts a new ledger transaction if the yearly credit hasn't
   * taken place.
   *
   * @param profileId The profile ID of the employee
   * @param year      The year for which to get the balance
   * @return AbsenceBalanceResponse containing balance details
   */
  @Transactional
  public AbsenceBalanceResponse handleAbsenceBalanceRequest(
      Long profileId,
      int year) {

    // Fetch the user for whom to get the balance
    User user = userRepository.findById(profileId)
        .orElseThrow(() -> new IllegalArgumentException(
            "User not found"));

    int balance = getVacationBalance(user, year);
    int nextYearBalance = getVacationBalance(user, year + 1);

    return AbsenceBalanceResponse.builder()
        .year(year)
        .vacationDaysAllowance(YEARLY_VACATION_ALLOWANCE)
        .vacationDaysBalance(balance)
        .vacationDaysBalanceNextYear(nextYearBalance)
        .build();
  }

  /**
   * Lists all absences visible to the user for a given year.
   *
   * @param user The employee
   * @param year The year for which to list absences
   * @return List of AbsenceDTOs
   */
  @Transactional(readOnly = true)
  public List<AbsenceDTO> handleListVisibleAbsencesForUser(
      User user,
      int year) {

    return requestRepository.findAllApprovedOrUserRequestsByYear(
            user.getId(),
            year)
        .stream()
        .map(requestMapper::toDto)
        .toList();
  }


  /**
   * Handles a modification of an absence from the manager. Only status change
   * is available.
   *
   * @param existingRequest The AS-IS absence request
   * @param clientRequest   The desired TO-BE absence request
   * @param newStatus       Status extract from the client request, for ease of
   *                        access
   * @return New, up-to-date, DTO
   * @throws AccessDeniedException When request is valid, but manager tries to
   *                               change dates or to PENDING
   */
  private AbsenceDTO handleManagerUpdate(
      AbsenceRequest existingRequest,
      AbsenceRequest clientRequest,
      AbsenceStatus newStatus) {
    // Manager cannot change dates
    if (!clientRequest.getStartDate().equals(existingRequest.getStartDate())
        || !clientRequest.getEndDate().equals(existingRequest.getEndDate())) {
      throw new AccessDeniedException(
          "Managers cannot change the dates of an absence request.");
    }

    // Manager can only approve or reject
    if (newStatus != AbsenceStatus.PENDING) {
      return handleAbsenceStatusChange(existingRequest, newStatus);
    } else {
      throw new AccessDeniedException(
          "Managers cannot change status to PENDING");
    }
  }


  /**
   * Handles a modification of an absence from the owner. Performs date, status
   * and reason updates and validations.
   *
   * @param existingRequest The AS-IS absence request
   * @param clientRequest   The desired TO-BE absence request
   * @param newStatus       Status extract from the client request, for ease of
   *                        access
   * @return New, up-to-date, DTO
   * @throws IllegalArgumentException When request is no longer PENDING.
   * @throws AccessDeniedException    When request is valid, but owner tries to
   *                                  self-approve
   */
  private AbsenceDTO handleOwnerUpdate(
      AbsenceRequest existingRequest,
      AbsenceRequest clientRequest,
      AbsenceStatus newStatus) {

    // Employee can only update PENDING requests
    if (existingRequest.getStatus() != AbsenceStatus.PENDING) {
      throw new IllegalArgumentException(
          "You can only update PENDING requests.");
    }

    // Employee cannot approve their own request
    if (newStatus == AbsenceStatus.APPROVED) {
      throw new AccessDeniedException(
          "Employees cannot approve their own requests.");
    }

    // Reason update is independent
    if (!(existingRequest.getReason().equals(clientRequest.getReason()))) {
      existingRequest.setReason(clientRequest.getReason());
    }

    // Handle Status change has priority and isolation vs date changes.
    if (newStatus == AbsenceStatus.REJECTED) {
      return handleAbsenceStatusChange(existingRequest, newStatus);
    }

    // Check if dates are changing
    boolean datesChanged =
        !existingRequest.getStartDate().equals(clientRequest.getStartDate()) ||
            !existingRequest.getEndDate().equals(clientRequest.getEndDate());

    if (datesChanged) {
      // Revert the old debit (Credit back the days)
      revertLedgerForRequest(existingRequest);

      // Update the request object with new dates temporarily for validation
      existingRequest.setStartDate(clientRequest.getStartDate());
      existingRequest.setEndDate(clientRequest.getEndDate());

      // Validate balance and create new debits
      validateAndDebitAbsenceRequest(existingRequest);
    }

    // Save and return the up-to-date request
    requestRepository.save(existingRequest);

    return requestMapper.toDto(existingRequest);
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
  private int calculateBusinessDaysForPeriod(LocalDate startDate,
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
