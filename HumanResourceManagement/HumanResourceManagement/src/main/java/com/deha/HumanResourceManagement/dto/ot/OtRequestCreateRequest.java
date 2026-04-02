package com.deha.HumanResourceManagement.dto.ot;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class OtRequestCreateRequest {
    @NotNull(message = "OT date is required")
    private LocalDate logDate;

    @NotBlank(message = "Reason is required")
    private String reason;
}

