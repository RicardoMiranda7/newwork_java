package com.newwork.backend.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.newwork.backend.BaseIntegrationTest;
import com.newwork.backend.model.User;
import com.newwork.backend.model.UserProfile;
import com.newwork.backend.repository.ProfileRepository;
import com.newwork.backend.repository.UserRepository;
import java.math.BigDecimal;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@ActiveProfiles("test")
@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProfileIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private MockMvc mockMvc;
  @Autowired
  private UserRepository userRepository;
  @Autowired
  private ProfileRepository profileRepository;

  private User employee;
  private User coworker;
  private UserProfile employeeProfile;

  @BeforeEach
  void setUp() {
    // 0. Clean DB to ensure test isolation
    profileRepository.deleteAll();
    userRepository.deleteAll();

    // 1. Create Employee (The Owner)
    employee = User.builder().email("emp@test.com").username("emp")
        .password("pass").build();
    userRepository.save(employee);

    employeeProfile = UserProfile.builder()
        .user(employee)
        .fullName("John Employee")
        .salary(new BigDecimal("75000.00")) // Sensitive Data
        .address("123 Private St")              // Sensitive Data
        .jobTitle("Dev")
        .build();
    profileRepository.save(employeeProfile);

    // 2. Create Coworker (The Viewer)
    coworker = User.builder().email("coworker@test.com").username("coworker")
        .password("pass").build();
    userRepository.save(coworker);
  }

  // Use AfterAll to clear all the db
  @AfterAll
  void clear() {
    profileRepository.deleteAll();
    userRepository.deleteAll();
  }

  @Test
  @WithUserDetails(value = "emp@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
  void testGetOwnProfile_ShouldReturnSensitiveData() throws Exception {
    // Act: Employee requests their own profile
    mockMvc.perform(get("/api/v1/profiles/" + employeeProfile.getId()))
        .andExpect(status().isOk())
        // Assert: Salary should be present
        .andExpect(jsonPath("$.data.salary").value(75000.00))
        .andExpect(jsonPath("$.data.address").value("123 Private St"));
  }

  @Test
  @WithUserDetails(value = "coworker@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION)
  void testGetCoworkerProfile_ShouldHideSensitiveData() throws Exception {
    // Act: Coworker requests Employee's profile
    mockMvc.perform(get("/api/v1/profiles/" + employeeProfile.getId()))
        .andExpect(status().isOk())
        // Assert: Sensitive fields should be missing
        .andExpect(jsonPath("$.data.fullName").value("John Employee"))
        .andExpect(jsonPath("$.data.salary").doesNotExist())
        .andExpect(jsonPath("$.data.address").doesNotExist());
  }
}