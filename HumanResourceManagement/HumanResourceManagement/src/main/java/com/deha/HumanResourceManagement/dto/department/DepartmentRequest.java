package com.deha.HumanResourceManagement.dto.department;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DepartmentRequest {
    @Column(name = "name", nullable = false, length = 100, unique = true)
    private String name;

    @Column(name = "description", nullable = true, length = 255, unique = false)
    private String description;
}
