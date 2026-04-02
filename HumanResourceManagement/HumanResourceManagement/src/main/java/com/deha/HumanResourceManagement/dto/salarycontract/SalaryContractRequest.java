package com.deha.HumanResourceManagement.dto.salarycontract;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class SalaryContractRequest {
    @NotNull(message = "User id is required")
    private UUID userId;

    @NotNull(message = "Base salary is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Base salary must be greater than zero")
    private BigDecimal baseSalary;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;

    private Long expectedVersion;
}

