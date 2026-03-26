package com.deha.HumanResourceManagement.dto.salarycontract;

import com.deha.HumanResourceManagement.entity.SalaryContract;
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
    private UUID userId;
    private String userName;
    private BigDecimal baseSalary;
    private LocalDate startDate;
    private LocalDate endDate;
    private ContractStatus status;

    public static SalaryContractResponse fromEntity(SalaryContract contract) {
        return new SalaryContractResponse(
                contract.getId(),
                contract.getUser() != null ? contract.getUser().getId() : null,
                contract.getUser() != null
                        ? (contract.getUser().getFirstName() + " " + contract.getUser().getLastName()).trim()
                        : null,
                contract.getBaseSalary(),
                contract.getStartDate(),
                contract.getEndDate(),
                resolveStatus(contract.getStartDate(), contract.getEndDate())
        );
    }

    private static ContractStatus resolveStatus(LocalDate startDate, LocalDate endDate) {
        LocalDate today = LocalDate.now();
        if (startDate != null && startDate.isAfter(today)) {
            return ContractStatus.FUTURE;
        }
        if (endDate != null && (endDate.isBefore(today) || endDate.isEqual(today))) {
            return ContractStatus.EXPIRED;
        }
        return ContractStatus.ACTIVE;
    }
}

