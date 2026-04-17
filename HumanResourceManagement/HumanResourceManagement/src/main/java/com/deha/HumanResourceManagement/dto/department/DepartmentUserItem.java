package com.deha.HumanResourceManagement.dto.department;

import com.deha.HumanResourceManagement.entity.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DepartmentUserItem {
    private UUID id;
    private Long version;
    private String firstName;
    private String lastName;
    private String email;
    private Role role;
    private boolean active;
    private LocalDateTime createdAt;
    private UUID positionId;
    private String positionName;
}
