package com.newwork.backend.integration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.newwork.backend.BaseIntegrationTest;
import com.newwork.backend.dto.AbsenceDTO;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@ActiveProfiles("test")
class AbsenceIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private MockMvc mockMvc; //Might have false positive

  @Autowired
  private UserRepository userRepository;
  @Autowired
  private ProfileRepository profileRepository;
  @Autowired
  private BankHolidayRepository bankHolidayRepository;
  @Autowired
  private AbsenceLedgerRepository ledgerRepository;
  @Autowired
  private AbsenceRequestRepository requestRepository;
  @Autowired
  private ObjectMapper objectMapper; // For converting Objects to JSON

  private User testUser;
  private UserProfile testProfile;

  @BeforeEach
  void setUp() {
    // 1. Clean DB to ensure test isolation
    ledgerRepository.deleteAll();
    requestRepository.deleteAll();
    bankHolidayRepository.deleteAll();
    profileRepository.deleteAll();
    userRepository.deleteAll();

    // 2. Create a Test User
    testUser = User.builder()
        .email("employee@test.com")
        .username("employee")
        .password("encodedPass")
        .build();
    userRepository.save(testUser);

    // 3. Create a Test Profile
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

    // 4. Create a Bank Holiday (e.g., New Year's Day)
    bankHolidayRepository.save(BankHoliday.builder()
        .date(LocalDate.of(2025, 1, 1))
        .name("New Year")
        .build());
  }

  @Test
  @WithUserDetails(value = "employee@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
    // Simulates a logged-in user with this email
  void testCreateAbsenceRequest_Success() throws Exception {
    // Arrange: Request 5 days (Mon-Fri)
    AbsenceDTO requestDto = new AbsenceDTO();
    requestDto.setStartDate(LocalDate.of(2025, 6, 2)); // Monday
    requestDto.setEndDate(LocalDate.of(2025, 6, 6));   // Friday
    requestDto.setReason("Vacation");

    // Act: Perform POST request
    mockMvc.perform(post("/api/v1/absences")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestDto)))
        .andExpect(status().isOk());

    // Assert: Check Ledger
    // Should have 2 entries: 1 for Initial Allowance (+25), 1 for Debit (-5)
    assertEquals(2, ledgerRepository.count());

    // Check Balance via API
    mockMvc.perform(get("/api/v1/absences/absence-balance")
            .param("id", testProfile.getId().toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.vacationDaysBalance").value(20));
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
    mockMvc.perform(post("/api/v1/absences")
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
    mockMvc.perform(post("/api/v1/absences")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(requestDto)))
        .andExpect(status().isOk());

    // Assert: Balance should be 23 (25 - 2)
    mockMvc.perform(get("/api/v1/absences/absence-balance")
            .param("id", testProfile.getId().toString()))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.vacationDaysBalance").value(23));
  }
}