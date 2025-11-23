package com.newwork.backend;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public abstract class BaseIntegrationTest {

  // Define the container.
  static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
      "postgres:17")
      .withDatabaseName("test_db")
      .withUsername("test_user")
      .withPassword("test_pass");

  static {
    postgres.start();
  }

  // Overwrite the application.properties to point to the Docker container
  @DynamicPropertySource
  static void configureProperties(DynamicPropertyRegistry registry) {
    registry.add("spring.datasource.url", postgres::getJdbcUrl);
    registry.add("spring.datasource.username", postgres::getUsername);
    registry.add("spring.datasource.password", postgres::getPassword);
    // Ensure Flyway runs on the test DB
    registry.add("spring.flyway.enabled", () -> "true");
  }
}