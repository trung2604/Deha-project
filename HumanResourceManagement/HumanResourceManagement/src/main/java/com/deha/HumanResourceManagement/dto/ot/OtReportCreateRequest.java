package com.deha.HumanResourceManagement.dto.ot;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.util.UUID;

@Data
public class OtReportCreateRequest {
    @NotNull(message = "OT session id is required")
    private UUID otSessionId;

    @NotNull(message = "Reported OT hours is required")
    @Positive(message = "Reported OT hours must be greater than 0")
    private Integer reportedOtHours;

    @NotBlank(message = "Report note (evidence) is required")
    private String reportNote;
}

