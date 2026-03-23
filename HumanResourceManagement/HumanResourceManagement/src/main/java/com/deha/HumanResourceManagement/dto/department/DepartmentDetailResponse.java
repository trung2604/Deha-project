package com.deha.HumanResourceManagement.dto.department;

import com.deha.HumanResourceManagement.entity.Department;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DepartmentDetailResponse {
    private UUID id;
    private String name;
    private String description;
    private UUID officeId;
    private String officeName;
    private List<DepartmentPositionItem> positions;
    private List<DepartmentUserItem> users;

    public static DepartmentDetailResponse fromEntity(Department department) {
        if (department == null) return null;
        return new DepartmentDetailResponse(
                department.getId(),
                department.getName(),
                department.getDescription(),
                department.getOffice() != null ? department.getOffice().getId() : null,
                department.getOffice() != null ? department.getOffice().getName() : null,
                department.getPositions().stream()
                        .map(DepartmentPositionItem::fromEntity)
                        .toList(),
                department.getUsers().stream()
                        .map(DepartmentUserItem::fromEntity)
                        .toList()
        );
    }
}

