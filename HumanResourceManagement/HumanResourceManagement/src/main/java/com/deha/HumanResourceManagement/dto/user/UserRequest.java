package com.deha.HumanResourceManagement.dto.user;

import com.deha.HumanResourceManagement.entity.Department;
import com.deha.HumanResourceManagement.entity.Office;
import com.deha.HumanResourceManagement.entity.Position;
import com.deha.HumanResourceManagement.entity.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRequest {
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

    private Department department;

    private Position position;

    @NotNull(message = "Office is required")
    private Office office;

    @NotNull(message = "Role cannot be blank")
    private Role role;
}
