package com.deha.HumanResourceManagement.service.payroll;

import com.deha.HumanResourceManagement.entity.AttendanceLog;
import com.deha.HumanResourceManagement.entity.enums.OtType;
import com.deha.HumanResourceManagement.strategy.OtTypeResolver;
import com.deha.HumanResourceManagement.strategy.OtTypeStrategy;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PayrollCalculatorTest {

    private final PayrollCalculator calculator = new PayrollCalculator(new OtTypeResolver(List.<OtTypeStrategy>of()));

    @Test
    void countWorkingDays_shouldExcludeWeekends() {
        int days = calculator.countWorkingDays(
                LocalDate.of(2026, 3, 1),
                LocalDate.of(2026, 3, 31)
        );
        assertEquals(22, days);
    }

    @Test
    void calculateRegularPay_shouldProRateByRegularHours() {
        BigDecimal regularPay = calculator.calculateRegularPay(new BigDecimal("22000000"), 88, 22, 8);
        assertEquals(new BigDecimal("11000000.00"), regularPay);
    }

    @Test
    void calculateOtPay_shouldUseOtMultiplier() {
        AttendanceLog log = new AttendanceLog();
        log.setLogDate(LocalDate.of(2026, 3, 10));
        log.setCheckInTime(LocalDateTime.of(2026, 3, 10, 9, 0));
        log.setOtHours(2);
        log.setOtType(OtType.WEEKDAY);

        BigDecimal otPay = calculator.calculateOtPayFromAttendanceLogs(
                List.of(log),
                new BigDecimal("22000000"),
                22,
                8,
                1.5d,
                2.0d,
                3.0d,
                0.3d
        );

        assertEquals(new BigDecimal("375000.00"), otPay);
    }

    @Test
    void sumRegularHours_shouldSumWorkedHours() {
        AttendanceLog first = new AttendanceLog();
        first.setWorkedHours(8);
        AttendanceLog second = new AttendanceLog();
        second.setWorkedHours(5);

        int regularHours = calculator.sumRegularHours(List.of(first, second));

        assertEquals(13, regularHours);
    }
}

