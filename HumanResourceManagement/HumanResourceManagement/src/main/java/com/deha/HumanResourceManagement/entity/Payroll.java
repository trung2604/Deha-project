package com.deha.HumanResourceManagement.entity;

import com.deha.HumanResourceManagement.entity.enums.PayrollStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "payrolls",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "pay_year", "pay_month"})
)
@Data
@EqualsAndHashCode(callSuper = false)
public class Payroll extends AuditableByUser {
    @Id
    @UuidGenerator
    private UUID id;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "office_id", nullable = false)
    private Office office;

    @Column(name = "pay_year", nullable = false)
    private Integer payYear;

    @Column(name = "pay_month", nullable = false)
    private Integer payMonth;

    @Column(name = "base_salary_snapshot", nullable = false, precision = 19, scale = 2)
    private BigDecimal baseSalarySnapshot;

    @Column(name = "working_days_in_month", nullable = false)
    private Integer workingDaysInMonth;

    @Column(name = "present_days", nullable = false)
    private Integer presentDays;

    @Column(name = "regular_hours", nullable = false)
    private Integer regularHours;

    @Column(name = "regular_pay", nullable = false, precision = 19, scale = 2)
    private BigDecimal regularPay;

    @Column(name = "ot_hours", nullable = false)
    private Integer otHours;

    @Column(name = "ot_pay", nullable = false, precision = 19, scale = 2)
    private BigDecimal otPay;

    @Column(name = "net_salary", nullable = false, precision = 19, scale = 2)
    private BigDecimal netSalary;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private PayrollStatus status;

    @Column(name = "generated_at", nullable = false)
    private LocalDateTime generatedAt;
}

