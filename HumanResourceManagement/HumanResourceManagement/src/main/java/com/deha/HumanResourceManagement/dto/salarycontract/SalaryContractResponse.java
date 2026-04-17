package com.deha.HumanResourceManagement.dto.salarycontract;

import com.deha.HumanResourceManagement.entity.enums.ContractStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalaryContractResponse {
    private UUID id;
    private Long version;
    private UUID userId;
    private String userName;
    private BigDecimal baseSalary;
    private LocalDate startDate;
    private LocalDate endDate;
    private ContractStatus status;
}
