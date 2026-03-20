package com.deha.HumanResourceManagement.dto.auth;

import com.deha.HumanResourceManagement.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileResponse {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private Role role;
    private boolean active;
    private UUID departmentId;
    private String departmentName;
    private UUID positionId;
    private String positionName;
}

