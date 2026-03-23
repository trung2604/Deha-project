package com.deha.HumanResourceManagement.dto.department;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DepartmentRequest {
    @Column(name = "name", nullable = false, length = 100, unique = true)
    private String name;

    @Column(name = "description", nullable = true, length = 255, unique = false)
    private String description;

    @NotNull(message = "Office is required")
    private UUID officeId;
}
