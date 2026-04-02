package com.deha.HumanResourceManagement.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateProfileRequest {
    @NotNull(message = "First name is required")
    @NotBlank(message = "First name cannot be blank")
    private String firstName;

    @NotNull(message = "Last name is required")
    @NotBlank(message = "Last name cannot be blank")
    private String lastName;

    @Pattern(regexp = "^$|^[0-9]{10}$", message = "Phone must be 10 digits")
    private String phone;

    private Long expectedVersion;
}
