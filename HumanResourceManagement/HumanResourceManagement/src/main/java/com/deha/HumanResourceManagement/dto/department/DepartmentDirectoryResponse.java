package com.deha.HumanResourceManagement.dto.department;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentDirectoryResponse {
    private List<DepartmentResponse> departments;
    private long totalCount;
}
