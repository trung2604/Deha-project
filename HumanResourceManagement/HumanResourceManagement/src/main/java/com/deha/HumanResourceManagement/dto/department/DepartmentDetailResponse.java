package com.deha.HumanResourceManagement.dto.department;

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
    private List<DepartmentPositionItem> positions;
    private List<DepartmentEmployeeItem> employees;
}

