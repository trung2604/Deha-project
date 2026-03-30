package com.deha.HumanResourceManagement.dto.position;

import com.deha.HumanResourceManagement.entity.Position;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PositionResponse {
    private UUID id;
    private Long version;
    private String name;
    private UUID departmentId;
    private String departmentName;
    private UUID officeId;
    private String officeName;

    public static PositionResponse fromEntity(Position position) {
        if (position == null) return null;
        return new PositionResponse(
                position.getId(),
                position.getVersion(),
                position.getName(),
                position.getDepartment() != null ? position.getDepartment().getId() : null,
                position.getDepartment() != null ? position.getDepartment().getName() : null,
                position.getDepartment() != null && position.getDepartment().getOffice() != null
                        ? position.getDepartment().getOffice().getId() : null,
                position.getDepartment() != null && position.getDepartment().getOffice() != null
                        ? position.getDepartment().getOffice().getName() : null
        );
    }
}
