package com.deha.HumanResourceManagement.dto.user;

import com.deha.HumanResourceManagement.entity.User;
import com.deha.HumanResourceManagement.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor  
public class UserResponse {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private UUID departmentId;
    private String departmentName;
    private UUID positionId;
    private String positionName;
    private Role role;
    private boolean active;
    private Date createdAt;

    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getPhone(),
                user.getDepartment() != null ? user.getDepartment().getId() : null,
                user.getDepartment() != null ? user.getDepartment().getName() : null,
                user.getPosition() != null ? user.getPosition().getId() : null,
                user.getPosition() != null ? user.getPosition().getName() : null,
                user.getRole(),
                user.isActive(),
                user.getCreatedAt()
        );
    }
}
