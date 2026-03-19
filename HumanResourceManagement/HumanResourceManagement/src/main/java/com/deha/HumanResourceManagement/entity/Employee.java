package com.deha.HumanResourceManagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "employees")
@Data
public class Employee {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(name = "first_name", nullable = false, length = 100)
    @NotNull(message = "First name cannot be null")
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    @NotNull(message = "Last name cannot be null")
    private String lastName;

    @Column(name = "email", nullable = false, length = 100, unique = true)
    @Email(message = "Email is not correct format")
    private String email;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id", nullable = false)
    private Position position;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Column(name = "created_at", nullable = false)
    private Date createdAt;
}