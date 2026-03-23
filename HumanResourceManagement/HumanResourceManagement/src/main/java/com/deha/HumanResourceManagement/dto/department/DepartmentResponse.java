package com.deha.HumanResourceManagement.dto.department;

import com.deha.HumanResourceManagement.entity.Department;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DepartmentResponse {

    private UUID id;
    private String name;
    private String description;
    private UUID officeId;
    private String officeName;

    public static DepartmentResponse fromEntity(Department department) {
        if (department == null) return null;
        return new DepartmentResponse(
                department.getId(),
                department.getName(),
                department.getDescription(),
                department.getOffice() != null ? department.getOffice().getId() : null,
                department.getOffice() != null ? department.getOffice().getName() : null
        );
    }
}
