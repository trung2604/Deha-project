package com.deha.HumanResourceManagement.entity;

import com.deha.HumanResourceManagement.exception.BadRequestException;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
public class User {

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

    @Column(name = "phone", nullable = true, length = 10)
    private String phone;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position_id")
    private Position position;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private Role role;

    @Column(name = "created_at", nullable = false)
    private Date createdAt;

    public void applyBasicInfo(String firstName, String lastName, String email, Role role) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
    }

    public void assignDepartmentAndPosition(Department department, Position position) {
        if (department == null || position == null) {
            throw new BadRequestException("Department and Position are required");
        }
        if (position.getDepartment() == null
                || position.getDepartment().getId() == null
                || !position.getDepartment().getId().equals(department.getId())) {
            throw new BadRequestException("Position does not belong to the specified department");
        }
        this.department = department;
        this.position = position;
    }

    public void activate() {
        this.isActive = true;
    }

    public void markCreatedNow() {
        this.createdAt = new Date();
    }
}