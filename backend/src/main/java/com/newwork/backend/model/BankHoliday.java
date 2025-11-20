package com.newwork.backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "bank_holidays")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BankHoliday {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private String name;

  @Column(nullable = false, unique = true)
  private LocalDate date;
}