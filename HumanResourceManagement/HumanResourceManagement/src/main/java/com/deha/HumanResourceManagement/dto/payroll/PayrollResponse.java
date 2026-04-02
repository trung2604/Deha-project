package com.deha.HumanResourceManagement.dto.payroll;

import com.deha.HumanResourceManagement.entity.Payroll;
import com.deha.HumanResourceManagement.entity.enums.PayrollStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PayrollResponse {
    private UUID id;
    private Long version;
    private UUID userId;
    private String userName;
    private UUID officeId;
    private Integer year;
    private Integer month;
    private BigDecimal baseSalarySnapshot;
    private Integer workingDaysInMonth;
    private Integer presentDays;
    private Integer regularHours;
    private BigDecimal regularPay;
    private Integer otHours;
    private BigDecimal otPay;
    private BigDecimal netSalary;
    private PayrollStatus status;
    private LocalDateTime generatedAt;

    public static PayrollResponse fromEntity(Payroll payroll) {
        return new PayrollResponse(
                payroll.getId(),
                payroll.getVersion(),
                payroll.getUser() != null ? payroll.getUser().getId() : null,
                payroll.getUser() != null ? (payroll.getUser().getFirstName() + " " + payroll.getUser().getLastName()).trim() : null,
                payroll.getOffice() != null ? payroll.getOffice().getId() : null,
                payroll.getPayYear(),
                payroll.getPayMonth(),
                payroll.getBaseSalarySnapshot(),
                payroll.getWorkingDaysInMonth(),
                payroll.getPresentDays(),
                payroll.getRegularHours(),
                payroll.getRegularPay(),
                payroll.getOtHours(),
                payroll.getOtPay(),
                payroll.getNetSalary(),
                payroll.getStatus(),
                payroll.getGeneratedAt()
        );
    }
}

