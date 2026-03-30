package com.deha.HumanResourceManagement.dto.department;

import com.deha.HumanResourceManagement.entity.Position;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DepartmentPositionItem {
    private UUID id;
    private Long version;
    private String name;

    public static DepartmentPositionItem fromEntity(Position position) {
        if (position == null) return null;
        return new DepartmentPositionItem(
                position.getId(),
                position.getVersion(),
                position.getName()
        );
    }
}

