package com.newwork.backend.integration;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.newwork.backend.BaseIntegrationTest;
import com.newwork.backend.dto.AbsenceDTO;
import com.newwork.backend.mapper.AbsenceMapper;
import com.newwork.backend.model.AbsenceLedger;
import com.newwork.backend.model.AbsenceRequest;
import com.newwork.backend.model.AbsenceStatus;
import com.newwork.backend.model.BankHoliday;
import com.newwork.backend.model.User;
import com.newwork.backend.model.UserProfile;
import com.newwork.backend.repository.AbsenceLedgerRepository;
import com.newwork.backend.repository.AbsenceRequestRepository;
import com.newwork.backend.repository.BankHolidayRepository;
import com.newwork.backend.repository.ProfileRepository;
import com.newwork.backend.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AbsenceIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private UserRepository userRepository;
  @Autowired
  private ProfileRepository profileRepository;
  @Autowired
  private BankHolidayRepository bankHolidayRepository;
  @Autowired
  private AbsenceLedgerRepository ledgerRepository;
  @Autowired
  private AbsenceRequestRepository absenceRequestRepository;
  @Autowired
  private ObjectMapper objectMapper; // For converting Objects to JSON
  @Autowired
  private AbsenceMapper requestMapper;

  private User manager;
  private User testUser;
  private UserProfile testProfile;


  // Use beforeAll for shared database state
  @BeforeAll
  void setUpOnce() {
    // 0. Clean user and profile
    userRepository.deleteAll();
    profileRepository.deleteAll();

    // 1. Create a Test User
    testUser = User.builder()
        .email("employee@test.com")
        .username("employee")
        .password("encodedPass")
        .build();
    userRepository.save(testUser);

    // 2. Create a Test Profile
    testProfile = UserProfile.builder()
        .user(testUser)
        .fullName("Test Employee")
        .jobTitle("Test Job")
        .bio("Test Bio")
        .address("123 Test St")
        .phoneNumber("1234567890")
        .salary(BigDecimal.valueOf(50000))
        .build();
    profileRepository.save(testProfile);

    //3. Add a Manager for permission tests
    manager = User.builder().
        email("manager@test.com").
        username("manager").
        password("pass").
        build();
    userRepository.save(manager);

    //3.1 Link employee to manager
    testProfile.setManager(manager);
    profileRepository.save(testProfile);

    // 4. Create a Bank Holiday (e.g., New Year's Day)
    bankHolidayRepository.save(BankHoliday.builder()
        .date(LocalDate.of(2025, 1, 1))
        .name("New Year")
        .build());
  }

  // Use beforeEach to ensure clean starting state
  @BeforeEach
  void setUp() {
    // 0. Clean DB to ensure test isolation
    ledgerRepository.deleteAll();
    absenceRequestRepository.deleteAll();
  }

  // WithUserDetails simulates a logged-in user with this email
  @Test
  @WithUserDetails(value = "employee@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
  void testCreateAbsenceRequest_Success() throws Exception {
    // Arrange: Request 5 days (Mon-Fri)
    AbsenceDTO requestDto = new AbsenceDTO();
    requestDto.setStartDate(LocalDate.of(2025, 6, 2)); // Monday
    requestDto.setEndDate(LocalDate.of(2025, 6, 6));   // Friday
    requestDto.setReason("Vacation");

    // Act: Perform POST request
    mockMvc.perform(post("/api/v1/absences/")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestDto)))
        .andExpect(status().isOk());

    // Assert: Check Ledger
    // Should have 2 entries: 1 for Initial Allowance (+25), 1 for Debit (-5)
    assertEquals(2, ledgerRepository.count());

    // Check Balance via API
    mockMvc.perform(get("/api/v1/absences/absence-balance?year=2025")
            .param("id", testProfile.getId().toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.vacationDaysBalance").value(20));
  }

  @Test
  @WithUserDetails(value = "employee@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
  void testCreateAbsenceRequest_InsufficientBalance() throws Exception {
    // Arrange: Request 30 days (more than 25 allowance)
    AbsenceDTO requestDto = new AbsenceDTO();
    requestDto.setStartDate(LocalDate.of(2025, 6, 1));
    requestDto.setEndDate(LocalDate.of(2025, 7, 15));
    requestDto.setReason("Long Vacation");

    // Act & Assert: Expect 400 Bad Request
    mockMvc.perform(post("/api/v1/absences/")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestDto)))
        .andExpect(
            status().isBadRequest()); // GlobalExceptionHandler should catch IllegalArgumentException
  }

  @Test
  @WithUserDetails(value = "employee@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
  void testCreateAbsenceRequest_WithBankHoliday() throws Exception {
    // Arrange: Request Jan 1st (Holiday) to Jan 3rd (Friday)
    // Jan 1 = Holiday, Jan 2 = Thu, Jan 3 = Fri. Total business days = 2.
    AbsenceDTO requestDto = new AbsenceDTO();
    requestDto.setStartDate(LocalDate.of(2025, 1, 1));
    requestDto.setEndDate(LocalDate.of(2025, 1, 3));
    requestDto.setReason("New Year Trip");

    // Act
    mockMvc.perform(post("/api/v1/absences/")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestDto)))
        .andExpect(status().isOk());

    // Assert: Balance should be 23 (25 - 2)
    mockMvc.perform(get("/api/v1/absences/absence-balance")
            .param("id", testProfile.getId().toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.vacationDaysBalance").value(23));
  }

  @Test
  @WithUserDetails(value = "employee@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
  void testCreateAbsenceRequest_Overlap_ShouldFail() throws Exception {
    // Arrange: Create an existing request for June 2-6
    AbsenceRequest existing = AbsenceRequest.builder()
        .employee(testUser)
        .startDate(LocalDate.of(2025, 6, 2))
        .endDate(LocalDate.of(2025, 6, 6))
        .status(AbsenceStatus.APPROVED)
        .reason("June vacation test")
        .build();
    absenceRequestRepository.save(existing);

    // Arrange: Try to create a new request that overlaps (June 5-9)
    AbsenceDTO overlapDto = new AbsenceDTO();
    overlapDto.setStartDate(LocalDate.of(2025, 6, 5));
    overlapDto.setEndDate(LocalDate.of(2025, 6, 9));
    overlapDto.setReason("Overlap Trip");

    // Act & Assert: Should return 400 Bad Request
    mockMvc.perform(post("/api/v1/absences/")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(overlapDto)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.message").value(containsString("overlaps")));
  }

  @Test
  @WithUserDetails(value = "manager@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
  void testUpdateStatus_AsManager_ShouldSuccess() throws Exception {
    // Arrange: Create a PENDING request for the employee
    AbsenceRequest request = AbsenceRequest.builder()
        .employee(testUser)
        .startDate(LocalDate.of(2025, 8, 1))
        .endDate(LocalDate.of(2025, 8, 5))
        .status(AbsenceStatus.PENDING)
        .reason("Summer vacation test")
        .build();
    request = absenceRequestRepository.save(request);

    // Act & Assert: Manager approves it
    var approvedRequest = request;
    approvedRequest.setStatus(AbsenceStatus.APPROVED);
    var dto = requestMapper.toDto(approvedRequest);

    mockMvc.perform(patch("/api/v1/absences/update-status")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.status").value("APPROVED"));
  }

  @Test
  @WithUserDetails(value = "employee@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
  void testApproveAbsence_AsEmployee_ShouldForbidden() throws Exception {
    // Arrange: Create a PENDING request
    AbsenceRequest request = AbsenceRequest.builder()
        .employee(testUser)
        .startDate(LocalDate.of(2025, 8, 1))
        .endDate(LocalDate.of(2025, 8, 5))
        .status(AbsenceStatus.PENDING)
        .reason("Summer vacation test")
        .build();
    request = absenceRequestRepository.save(request);

    // Arrange: Prepare the approval request dto
    var approvedRequest = request;
    approvedRequest.setStatus(AbsenceStatus.APPROVED);
    var dto = requestMapper.toDto(approvedRequest);

    // Act: Employee tries to approve their own request
    mockMvc.perform(patch("/api/v1/absences/update-status")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(dto)))
        .andExpect(status().isForbidden()); // Should be 403
  }

  @Test
  @WithUserDetails("manager@test.com")
  void testUpdate_ManagerCannotChangeDates_ShouldFail() throws Exception {
    // Arrange: Create a PENDING request
    AbsenceRequest request = AbsenceRequest.builder()
        .employee(testUser)
        .startDate(LocalDate.of(2025, 8, 1))
        .endDate(LocalDate.of(2025, 8, 5))
        .status(AbsenceStatus.PENDING)
        .reason("Summer vacation test")
        .build();
    request = absenceRequestRepository.save(request);

    // Act: Manager tries to Approve BUT also changes dates
    AbsenceDTO updateDto = new AbsenceDTO();
    updateDto.setId(request.getId());
    updateDto.setEmployee(testUser.getEmail());
    updateDto.setStartDate(LocalDate.of(2025, 8, 2));
    updateDto.setEndDate(LocalDate.of(2025, 8, 10)); // Changed!
    updateDto.setReason("Approved but longer");
    updateDto.setStatus(AbsenceStatus.APPROVED);

    // Assert: Expect 403 Forbidden (AccessDeniedException)
    mockMvc.perform(patch("/api/v1/absences/update-status")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateDto)))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.details").value(
            "Managers cannot change the dates of an absence request."));
  }

  @Test
  @WithUserDetails("employee@test.com")
  void testUpdate_AlreadyRejected_ShouldFail() throws Exception {
    // Arrange: Create a REJECTED request
    AbsenceRequest request = AbsenceRequest.builder()
        .employee(testUser)
        .startDate(LocalDate.of(2025, 8, 1))
        .endDate(LocalDate.of(2025, 8, 5))
        .status(AbsenceStatus.REJECTED)
        .reason("Summer vacation test")
        .build();
    request = absenceRequestRepository.save(request);

    // Act: Try to update it
    AbsenceDTO updateDto = new AbsenceDTO();
    updateDto.setId(request.getId());
    updateDto.setEmployee(testUser.getEmail());
    updateDto.setStartDate(LocalDate.of(2025, 8, 1));
    updateDto.setEndDate(LocalDate.of(2025, 8, 5));
    updateDto.setReason("Trying to fix it");
    updateDto.setStatus(AbsenceStatus.PENDING);

    // Assert: Expect 400 Bad Request (IllegalArgumentException)
    mockMvc.perform(patch("/api/v1/absences/update-status")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateDto)))
        .andExpect(status().isBadRequest())
        .andExpect(
            jsonPath("$.details").value(containsString("already rejected")));
  }

  @Test
  @WithUserDetails("employee@test.com")
  void testUpdate_OwnerChangeDates_InsufficientBalance_ShouldFail()
      throws Exception {

    // 0. Setup: User has 25 days allowance.
    // 1. Record a penalty of one day
    var penaltyTrx = AbsenceLedger.builder()
        .employee(testUser)
        .year(2025)
        .amount(-1)
        .description("Penalty")
        .build();
    ledgerRepository.save(penaltyTrx);

    // 2. Create a request to drain the allowance
    AbsenceRequest drainAbs = AbsenceRequest.builder()
        .employee(testUser)
        .startDate(LocalDate.of(2025, 1, 1))
        .endDate(LocalDate.of(2025, 2, 4))
        .status(AbsenceStatus.PENDING)
        .reason("Ski vacation - drain")
        .build();
    absenceRequestRepository.save(drainAbs);

    // 2. Drain to zero with a single debit
    var drainTrx = AbsenceLedger.builder()
        .employee(testUser)
        .absenceRequest(drainAbs)
        .year(2025)
        .amount(-24)
        .description("Ski vacation - drain")
        .build();
    ledgerRepository.save(drainTrx);

    // Act: Update the request from 24 to a higher amount (31 days)
    AbsenceDTO updateDto = new AbsenceDTO();
    updateDto.setId(drainAbs.getId());
    updateDto.setEmployee(testUser.getEmail());
    updateDto.setStartDate(LocalDate.of(2025, 1, 1));
    updateDto.setEndDate(LocalDate.of(2025, 2, 13));
    updateDto.setReason("Extending");
    updateDto.setStatus(AbsenceStatus.PENDING);

    // Assert:
    // The logic should be:
    // 1. Revert -24 (Balance becomes 24)
    // 2. Try to debit 31 vs 24
    // 3. Fail
    mockMvc.perform(patch("/api/v1/absences/update-status")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateDto)))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.details").value(
            containsString("request is for 31 business days")));
  }

  @Test
  @WithUserDetails("employee@test.com")
  void testUpdate_OwnerChangeDates_Success_ValidateLedger()
      throws Exception {

    // 0. Setup: User has 25 days allowance.
    // 1. Record a penalty of one day
    var penaltyTrx = AbsenceLedger.builder()
        .employee(testUser)
        .year(2025)
        .amount(-1)
        .description("Penalty")
        .build();
    ledgerRepository.save(penaltyTrx);

    // 2. Create a request to drain the allowance
    AbsenceRequest drainAbs = AbsenceRequest.builder()
        .employee(testUser)
        .startDate(LocalDate.of(2025, 1, 1))
        .endDate(LocalDate.of(2025, 2, 4))
        .status(AbsenceStatus.PENDING)
        .reason("Ski vacation - drain")
        .build();
    absenceRequestRepository.save(drainAbs);

    // 2. Drain to zero with a single debit
    var drainTrx = AbsenceLedger.builder()
        .employee(testUser)
        .absenceRequest(drainAbs)
        .year(2025)
        .amount(-24)
        .description("Ski vacation - drain")
        .build();
    ledgerRepository.save(drainTrx);

    // Act: Update the request from 24 to a smaller amount (23 days)
    AbsenceDTO updateDto = new AbsenceDTO();
    updateDto.setId(drainAbs.getId());
    updateDto.setEmployee(testUser.getEmail());
    updateDto.setStartDate(LocalDate.of(2025, 1, 1));
    updateDto.setEndDate(LocalDate.of(2025, 2, 3));
    updateDto.setReason("Reducing");
    updateDto.setStatus(AbsenceStatus.PENDING);

    // Assert:
    // The logic should be:
    // 1. Revert -24 (Balance becomes 24)
    // 2. Try to debit 23 vs 24
    // 3. Success
    mockMvc.perform(patch("/api/v1/absences/update-status")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(updateDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.endDate").value("2025-02-03"));

    // Assert remaining balance as well
    mockMvc.perform(get("/api/v1/absences/absence-balance")
            .param("id", testProfile.getId().toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.vacationDaysBalance").value(1));
  }

}