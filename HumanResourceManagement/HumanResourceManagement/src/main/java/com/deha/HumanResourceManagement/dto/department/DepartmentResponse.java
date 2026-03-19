package com.deha.HumanResourceManagement.dto.department;


import com.deha.HumanResourceManagement.dto.employee.EmployeeResponse;
import com.deha.HumanResourceManagement.entity.Department;
import com.deha.HumanResourceManagement.entity.Employee;
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
}
