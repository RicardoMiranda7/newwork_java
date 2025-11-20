package com.newwork.backend.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

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
  @JoinColumn(name = "user_id", nullable = false, unique = true) // To deal with one-to-one relationship and n+1 problem
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