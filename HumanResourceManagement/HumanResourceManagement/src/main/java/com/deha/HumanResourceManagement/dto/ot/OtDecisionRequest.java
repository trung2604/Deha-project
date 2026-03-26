package com.deha.HumanResourceManagement.dto.ot;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OtDecisionRequest {
    @NotNull(message = "Approved flag is required")
    private Boolean approved;
    private String decisionNote;
}

