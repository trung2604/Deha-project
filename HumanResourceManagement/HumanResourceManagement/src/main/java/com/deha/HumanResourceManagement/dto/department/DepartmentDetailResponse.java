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
    private Long version;
    private String name;
    private String description;
    private UUID officeId;
    private String officeName;
    private List<DepartmentPositionItem> positions;
    private List<DepartmentUserItem> users;
}
