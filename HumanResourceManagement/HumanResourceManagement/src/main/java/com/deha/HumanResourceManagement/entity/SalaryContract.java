package com.deha.HumanResourceManagement.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "salary_contracts")
@Data
public class SalaryContract {
    @Id
    @UuidGenerator
    private UUID id;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "base_salary", nullable = false, precision = 19, scale = 2)
    private BigDecimal baseSalary;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    public boolean overlaps(LocalDate fromDate, LocalDate toDate) {
        LocalDate effectiveEnd = this.endDate == null ? LocalDate.of(9999, 12, 31) : this.endDate;
        return !this.startDate.isAfter(toDate) && !effectiveEnd.isBefore(fromDate);
    }
}

