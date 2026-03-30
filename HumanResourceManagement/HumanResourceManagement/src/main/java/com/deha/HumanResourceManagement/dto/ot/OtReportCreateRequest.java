package com.deha.HumanResourceManagement.dto.ot;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class OtReportCreateRequest {
    @NotNull(message = "OT session id is required")
    private UUID otSessionId;


    @NotBlank(message = "Report note (evidence) is required")
    @Size(min = 10, max = 500, message = "Report note must be between 10 and 500 characters")
    private String reportNote;
}

