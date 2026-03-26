package com.deha.HumanResourceManagement.dto.auth;

import com.deha.HumanResourceManagement.entity.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LoginResponse {
    private String token;
    private UUID userId;
    private String email;
    private Role role;
}

