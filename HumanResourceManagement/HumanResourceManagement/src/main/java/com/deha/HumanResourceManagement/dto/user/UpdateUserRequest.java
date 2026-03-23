package com.deha.HumanResourceManagement.dto.user;

import com.deha.HumanResourceManagement.entity.Department;
import com.deha.HumanResourceManagement.entity.Office;
import com.deha.HumanResourceManagement.entity.Position;
import com.deha.HumanResourceManagement.entity.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateUserRequest {
    @NotNull(message = "First name is required")
    @NotBlank(message = "First name cannot be blank")
    private String firstName;

    @NotNull(message = "last name is required")
    @NotBlank(message = "Last name cannot be blank")
    private String lastName;

    @NotNull(message = "Email is required")
    @Email(message = "Email is not correct format")
    private String email;

    private String phone;

    private Department department;

    private Position position;

    @NotNull(message = "Office is required")
    private Office office;

    @NotNull(message = "Role cannot be blank")
    private Role role;
}
