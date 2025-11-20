package com.newwork.backend.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "absence_ledger")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AbsenceLedger {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "employee_id", nullable = false)
  private User employee;

  @ManyToOne
  @JoinColumn(name = "absence_request_id")
  private AbsenceRequest absenceRequest;

  @Column(nullable = false)
  private Integer amount;

  @Column(nullable = false)
  private Integer year;

  private String description;

  @CreationTimestamp
  @Column(name = "created_at")
  private LocalDateTime createdAt;
}