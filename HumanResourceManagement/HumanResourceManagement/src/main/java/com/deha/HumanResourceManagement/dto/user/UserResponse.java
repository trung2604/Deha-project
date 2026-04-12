package com.deha.HumanResourceManagement.dto.user;

import com.deha.HumanResourceManagement.entity.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponse {
    private UUID id;
    private Long version;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String avatarUrl;
    private UUID officeId;
    private String officeName;
    private UUID departmentId;
    private String departmentName;
    private UUID positionId;
    private String positionName;
    private Role role;
    private boolean active;
    private LocalDateTime createdAt;
}
