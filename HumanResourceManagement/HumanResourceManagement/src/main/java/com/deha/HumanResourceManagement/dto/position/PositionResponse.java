package com.deha.HumanResourceManagement.dto.position;

import com.deha.HumanResourceManagement.entity.Department;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PositionResponse {
    private UUID id;
    private String name;
    private Department department;
}
