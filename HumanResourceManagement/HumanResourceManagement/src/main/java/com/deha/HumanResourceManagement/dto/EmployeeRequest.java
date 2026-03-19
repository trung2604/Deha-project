package com.deha.HumanResourceManagement.dto;

import com.deha.HumanResourceManagement.entity.Department;
import com.deha.HumanResourceManagement.entity.Position;
import com.deha.HumanResourceManagement.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EmployeeRequest {
    @NotNull(message = "First name is required")
    @NotBlank(message = "First name cannot be blank")
    private String firstName;

    @NotNull(message = "last name is required")
    @NotBlank(message = "Last name cannot be blank")
    private String lastName;

    @NotNull(message = "Email is required")
    @Email(message = "Email is not correct format")
    private String email;

    @NotBlank(message = "Password cannot be blank")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "Department cannot be blank")
    private Department department;

    @NotBlank(message = "Position cannot be blank")
    private Position position;

    @NotNull(message = "Role cannot be blank")
    private Role role;
}
