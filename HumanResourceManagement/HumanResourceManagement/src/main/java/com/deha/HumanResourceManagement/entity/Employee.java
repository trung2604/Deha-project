package com.deha.HumanResourceManagement.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "employees")
@Data
public class Employee {
    @Id
    @UuidGenerator
    private UUID id;

    @Column(name="first_name", nullable = false, length = 100, unique = false)
    @NotNull(message = "First name cannot be null")
    private String firstName;

    @Column(name="last_name", nullable = false, length = 100, unique = false)
    @NotNull(message = "Last name cannot be null")
    private String lastName;

    @Column(name="email", nullable = false, length = 100, unique = true)
    @Email(message = "Email is not correct format")
    private String email;

    @Column(name="password", nullable = false, length = 255, unique = false)
    private String password;

    @Column(name="is_active", nullable = false)
    private boolean isActive;

    @JoinColumn(name = "department_id", referencedColumnName = "id", nullable = true)
    @ManyToOne(fetch = FetchType.LAZY)
    private Department department;

    @OneToMany(mappedBy = "employee")
    private List<EmployeePosition> position;

    @Column(name = "role", nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(name = "created_at", nullable = false)
    private Date createdAt;
}
