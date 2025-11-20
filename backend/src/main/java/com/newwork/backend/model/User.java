package com.newwork.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

@Entity // Marks this class as a JPA entity
@Table(name = "users") // Specifies the table name in the database, avoid problems with reserved keywords
@Data // Generates Getters, Setters, toString, equals, hashCode
@NoArgsConstructor // Required for JPA, generates a no-args constructor
@AllArgsConstructor // Required for Builder, generates an all-args constructor. Allows  to create objects like User.builder().email("...").build().
@Builder // Implements the Builder pattern for easy object creation
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private String email;

  @Column(nullable = false)
  private String username;

  @Column(nullable = false)
  private String password;

  @Builder.Default
  @Column(name = "is_active")
  private boolean isActive = true;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;
}