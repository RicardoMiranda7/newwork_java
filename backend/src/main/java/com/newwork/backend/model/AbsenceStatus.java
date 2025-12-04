package com.newwork.backend.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum AbsenceStatus {
  PENDING("PENDING"),
  APPROVED("APPROVED"),
  REJECTED("REJECTED");

  private final String description;
}