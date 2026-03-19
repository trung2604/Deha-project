package com.deha.HumanResourceManagement.dto;

import com.deha.HumanResourceManagement.entity.Employee;
import com.deha.HumanResourceManagement.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor  
public class EmployeeResponse {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private Role role;
    private Date createdAt;

    public static EmployeeResponse fromEntity(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                employee.getFirstName(),
                employee.getLastName(),
                employee.getEmail(),
                employee.getRole(),
                employee.getCreatedAt()
        );
    }
}
