package com.deha.HumanResourceManagement.dto.position;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PositionRequest {
    @NotBlank(message = "Position name is required")
    @Size(max = 100, message = "Position name must be at most 100 characters")
    private String name;
}
