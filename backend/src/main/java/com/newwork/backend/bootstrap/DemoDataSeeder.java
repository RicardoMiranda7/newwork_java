package com.newwork.backend.bootstrap;

import com.newwork.backend.model.AbsenceLedger;
import com.newwork.backend.model.AbsenceRequest;
import com.newwork.backend.model.AbsenceStatus;
import com.newwork.backend.model.BankHoliday;
import com.newwork.backend.model.Profile;
import com.newwork.backend.model.User;
import com.newwork.backend.repository.AbsenceLedgerRepository;
import com.newwork.backend.repository.AbsenceRequestRepository;
import com.newwork.backend.repository.BankHolidayRepository;
import com.newwork.backend.repository.ProfileRepository;
import com.newwork.backend.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j // Lombok annotation for Logging
public class DemoDataSeeder implements CommandLineRunner {

  private final UserRepository userRepository;
  private final ProfileRepository profileRepository;
  private final BankHolidayRepository bankHolidayRepository;
  private final AbsenceRequestRepository absenceRequestRepository;
  private final AbsenceLedgerRepository absenceLedgerRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional
  public void run(String... args) throws Exception {
    log.info("Starting Demo Data Seeding...");

    // 1. Create Manager
    User manager = createUserIfNotFound("manager@example.com", "manager",
        "password123");

    // Manager Profile 
    createProfileIfNotFound(manager, null, "Manager User", "Manager",
        new BigDecimal("90000.00"), "Management");

    // 2. Create Employee (John Smith)
    User johnSmith = createUserIfNotFound("john.smith@example.com", "johnsmith",
        "password123");
    createProfileIfNotFound(johnSmith, manager, "John Smith",
        "Software Developer", new BigDecimal("75000.00"), "Engineering");

    // 3. Create Co-worker (John Doe)
    User johnDoe = createUserIfNotFound("john.doe@example.com", "johndoe",
        "password123");
    createProfileIfNotFound(johnDoe, manager, "John Doe", "Business Analyst",
        new BigDecimal("75000.00"), "Business");

    log.info("User and profile data generated successfully!");

    // 4. Generate Holidays
    seedHolidays();

    // 5. Add one winter vacation
    AbsenceRequest absenceRequest = AbsenceRequest.builder()
        .employee(johnSmith)
        .startDate(LocalDate.of(2025, 12, 22))
        .endDate(LocalDate.of(2025, 12, 29))
        .status(AbsenceStatus.APPROVED)
        .reason("Winter Vacation")
        .build();
    absenceRequestRepository.save(absenceRequest);

    AbsenceLedger absenceLedger = AbsenceLedger.builder()
        .employee(johnSmith)
        .absenceRequest(absenceRequest)
        .year(2025)
        .amount(-5)
        .description("Winter Vacation 2025")
        .build();
    absenceLedgerRepository.save(absenceLedger);
    log.info("Absence request and ledger entry created.");

    log.info("Demo data generation complete!");
  }

  private User createUserIfNotFound(String email, String username,
      String rawPassword) {
    return userRepository.findByEmail(email).orElseGet(() -> {
      User user = User.builder()
          .email(email)
          .username(username)
          .password(passwordEncoder.encode(rawPassword)) // BCrypt encryption
          .build();
      return userRepository.save(user);
    });
  }

  private void createProfileIfNotFound(User user, User manager, String fullName,
      String jobTitle, BigDecimal salary, String department) {
    if (profileRepository.findByUserId(user.getId()).isEmpty()) {
      Profile profile = Profile.builder()
          .user(user)
          .manager(manager)
          .fullName(fullName)
          .jobTitle(jobTitle)
          .salary(salary)
          .department(department)
          .gender("Male") // Defaulting based on your python script
          .dateOfBirth(LocalDate.of(1990, 1, 1))
          .address("123 Main St, City")
          .phoneNumber("555-0123")
          .bio("Experienced " + jobTitle.toLowerCase())
          .joinedAt(LocalDateTime.now())
          .build();
      profileRepository.save(profile);
    }
  }

  private void seedHolidays() {
    log.info("Generating bank holidays for Portugal (2025-2026)...");

    Map<LocalDate, String> holidays = new HashMap<>();

    // 2025 Holidays (Portugal)
    holidays.put(LocalDate.of(2025, 1, 1), "New Year's Day");
    holidays.put(LocalDate.of(2025, 4, 18), "Good Friday");
    holidays.put(LocalDate.of(2025, 4, 20), "Easter Sunday");
    holidays.put(LocalDate.of(2025, 4, 25), "Freedom Day");
    holidays.put(LocalDate.of(2025, 5, 1), "Labor Day");
    holidays.put(LocalDate.of(2025, 6, 10), "Portugal Day");
    holidays.put(LocalDate.of(2025, 6, 19), "Corpus Christi");
    holidays.put(LocalDate.of(2025, 8, 15), "Assumption of Mary");
    holidays.put(LocalDate.of(2025, 10, 5), "Republic Day");
    holidays.put(LocalDate.of(2025, 11, 1), "All Saints' Day");
    holidays.put(LocalDate.of(2025, 12, 1), "Restoration of Independence");
    holidays.put(LocalDate.of(2025, 12, 8), "Immaculate Conception");
    holidays.put(LocalDate.of(2025, 12, 25), "Christmas Day");

    // 2026 Holidays (Portugal)
    holidays.put(LocalDate.of(2026, 1, 1), "New Year's Day");
    holidays.put(LocalDate.of(2026, 4, 3), "Good Friday");
    holidays.put(LocalDate.of(2026, 4, 5), "Easter Sunday");
    holidays.put(LocalDate.of(2026, 4, 25), "Freedom Day");
    holidays.put(LocalDate.of(2026, 5, 1), "Labor Day");
    holidays.put(LocalDate.of(2026, 6, 4), "Corpus Christi");
    holidays.put(LocalDate.of(2026, 6, 10), "Portugal Day");
    holidays.put(LocalDate.of(2026, 8, 15), "Assumption of Mary");
    holidays.put(LocalDate.of(2026, 10, 5), "Republic Day");
    holidays.put(LocalDate.of(2026, 11, 1), "All Saints' Day");
    holidays.put(LocalDate.of(2026, 12, 1), "Restoration of Independence");
    holidays.put(LocalDate.of(2026, 12, 8), "Immaculate Conception");
    holidays.put(LocalDate.of(2026, 12, 25), "Christmas Day");

    int count = 0;
    for (Map.Entry<LocalDate, String> entry : holidays.entrySet()) {
      if (!bankHolidayRepository.existsByDate(entry.getKey())) {
        bankHolidayRepository.save(BankHoliday.builder()
            .date(entry.getKey())
            .name(entry.getValue())
            .build());
        count++;
      }
    }
    log.info("{} new bank holidays created.", count);
  }
}