package com.deha.HumanResourceManagement.dto.department;

import com.deha.HumanResourceManagement.entity.enums.Role;
import com.deha.HumanResourceManagement.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;
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

    public static DepartmentUserItem fromEntity(User user) {
        if (user == null) return null;
        return new DepartmentUserItem(
                user.getId(),
                user.getVersion(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt(),
                user.getPosition() != null ? user.getPosition().getId() : null,
                user.getPosition() != null ? user.getPosition().getName() : null
        );
    }
}

