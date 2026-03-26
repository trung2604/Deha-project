package com.deha.HumanResourceManagement.service.payroll;

import com.deha.HumanResourceManagement.entity.AttendanceLog;
import com.deha.HumanResourceManagement.entity.OtReport;
import com.deha.HumanResourceManagement.entity.enums.OtType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Component
public class PayrollCalculator {
    public int countWorkingDays(LocalDate fromDate, LocalDate toDate) {
        int count = 0;
        for (LocalDate d = fromDate; !d.isAfter(toDate); d = d.plusDays(1)) {
            DayOfWeek day = d.getDayOfWeek();
            if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                count++;
            }
        }
        return count;
    }

    public int countPresentDays(List<AttendanceLog> logs) {
        if (logs == null || logs.isEmpty()) return 0;
        return (int) logs.stream()
                .filter(l -> l.getCheckInTime() != null)
                .map(AttendanceLog::getLogDate)
                .distinct()
                .count();
    }

    public int sumOtHours(List<AttendanceLog> logs) {
        if (logs == null || logs.isEmpty()) return 0;
        return logs.stream()
                .mapToInt(l -> l.getOtHours() == null ? 0 : l.getOtHours())
                .sum();
    }

    public int sumRegularHours(List<AttendanceLog> logs) {
        if (logs == null || logs.isEmpty()) return 0;
        return logs.stream()
                .mapToInt(l -> l.getWorkedHours() == null ? 0 : l.getWorkedHours())
                .sum();
    }

    public BigDecimal calculateRegularPay(BigDecimal baseSalary, int regularHours, int workingDaysInMonth, int standardWorkHours) {
        if (baseSalary == null || workingDaysInMonth <= 0 || standardWorkHours <= 0 || regularHours <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal hourlyRate = baseSalary
                .divide(BigDecimal.valueOf((long) workingDaysInMonth * standardWorkHours), 6, RoundingMode.HALF_UP);
        return hourlyRate.multiply(BigDecimal.valueOf(regularHours)).setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateOtPay(List<AttendanceLog> logs, BigDecimal baseSalary, int workingDaysInMonth, int standardWorkHours) {
        return calculateOtPayFromAttendanceLogs(logs, baseSalary, workingDaysInMonth, standardWorkHours, 1.5d, 2.0d, 3.0d, 0.3d);
    }

    public BigDecimal calculateOtPayFromAttendanceLogs(
            List<AttendanceLog> logs,
            BigDecimal baseSalary,
            int workingDaysInMonth,
            int standardWorkHours,
            double weekdayMultiplier,
            double weekendMultiplier,
            double holidayMultiplier,
            double nightBonusMultiplier
    ) {
        if (logs == null || logs.isEmpty() || baseSalary == null || workingDaysInMonth <= 0 || standardWorkHours <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal hourlyRate = baseSalary
                .divide(BigDecimal.valueOf((long) workingDaysInMonth * standardWorkHours), 6, RoundingMode.HALF_UP);

        BigDecimal otPay = BigDecimal.ZERO;
        for (AttendanceLog log : logs) {
            int otHours = log.getOtHours() == null ? 0 : log.getOtHours();
            if (otHours <= 0) continue;
            OtType otType = log.getOtType();
            BigDecimal multiplier = BigDecimal.valueOf(resolveMultiplier(
                    otType,
                    weekdayMultiplier,
                    weekendMultiplier,
                    holidayMultiplier,
                    nightBonusMultiplier
            ));
            otPay = otPay.add(hourlyRate.multiply(BigDecimal.valueOf(otHours)).multiply(multiplier));
        }
        return otPay.setScale(2, RoundingMode.HALF_UP);
    }

    public BigDecimal calculateOtPay(
            List<OtReport> reports,
            BigDecimal baseSalary,
            int workingDaysInMonth,
            int standardWorkHours,
            double weekdayMultiplier,
            double weekendMultiplier,
            double holidayMultiplier,
            double nightBonusMultiplier
    ) {
        if (reports == null || reports.isEmpty() || baseSalary == null || workingDaysInMonth <= 0 || standardWorkHours <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal hourlyRate = baseSalary
                .divide(BigDecimal.valueOf((long) workingDaysInMonth * standardWorkHours), 6, RoundingMode.HALF_UP);

        BigDecimal otPay = BigDecimal.ZERO;
        for (OtReport report : reports) {
            int otHours = report.getReportedOtHours() == null ? 0 : report.getReportedOtHours();
            if (otHours <= 0) continue;
            OtType otType = report.getAttendanceLog() != null ? report.getAttendanceLog().getOtType() : null;
            BigDecimal multiplier = BigDecimal.valueOf(resolveMultiplier(
                    otType,
                    weekdayMultiplier,
                    weekendMultiplier,
                    holidayMultiplier,
                    nightBonusMultiplier
            ));
            otPay = otPay.add(hourlyRate.multiply(BigDecimal.valueOf(otHours)).multiply(multiplier));
        }
        return otPay.setScale(2, RoundingMode.HALF_UP);
    }

    private double resolveMultiplier(
            OtType otType,
            double weekdayMultiplier,
            double weekendMultiplier,
            double holidayMultiplier,
            double nightBonusMultiplier
    ) {
        if (otType == null) return weekdayMultiplier;
        return switch (otType) {
            case WEEKDAY -> weekdayMultiplier;
            case NIGHT_WEEKDAY -> weekdayMultiplier + nightBonusMultiplier;
            case WEEKEND -> weekendMultiplier;
            case NIGHT_WEEKEND -> weekendMultiplier + nightBonusMultiplier;
            case HOLIDAY -> holidayMultiplier;
            case NIGHT_HOLIDAY -> holidayMultiplier + nightBonusMultiplier;
        };
    }
}

