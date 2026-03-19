package com.deha.HumanResourceManagement.dto.department;

import com.deha.HumanResourceManagement.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DepartmentEmployeeItem {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private Role role;
    private boolean active;
    private Date createdAt;
    private UUID positionId;
    private String positionName;
}

