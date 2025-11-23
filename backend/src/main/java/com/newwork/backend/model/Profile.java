package com.newwork.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profile {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private User user;

  @Column(name = "full_name", nullable = false)
  private String fullName;

  @Column(name = "job_title", nullable = false)
  private String jobTitle;

  // Sensitive Data
  private BigDecimal salary;
  private String gender;
  @Column(name = "date_of_birth")
  private LocalDate dateOfBirth;
  private String address;
  @Column(name = "phone_number")
  private String phoneNumber;

  // Non-sensitive
  private String bio;

  @CreationTimestamp
  @Column(name = "joined_at")
  private LocalDateTime joinedAt;

  private String department;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "manager_id")
  private User manager;
}