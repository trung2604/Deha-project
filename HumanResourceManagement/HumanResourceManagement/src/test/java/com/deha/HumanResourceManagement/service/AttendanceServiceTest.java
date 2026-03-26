package com.deha.HumanResourceManagement.service;

import com.deha.HumanResourceManagement.entity.AttendanceLog;
import com.deha.HumanResourceManagement.entity.Office;
import com.deha.HumanResourceManagement.strategy.OtTypeResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AttendanceServiceTest {

    private AttendanceService attendanceService;

    @BeforeEach
    void setUp() {
        attendanceService = new AttendanceService(
                null,
                null,
                new OtTypeResolver(List.of()),
                new AccessScopeService(null)
        );
    }

    @Test
    void totalHours_shouldNotCapAtShiftEnd() {
        LocalDateTime checkIn = LocalDateTime.of(2026, 3, 24, 8, 0);
        LocalDateTime checkOut = LocalDateTime.of(2026, 3, 24, 20, 30);

        int totalHours = attendanceService.calculateTotalWorkedHours(checkIn, checkOut);

        assertEquals(12, totalHours);
    }

    @Test
    void totalHours_shouldNotApplyLatePenalty() {
        LocalDateTime checkIn = LocalDateTime.of(2026, 3, 24, 9, 1);
        LocalDateTime checkOut = LocalDateTime.of(2026, 3, 24, 18, 0);

        int totalHours = attendanceService.calculateTotalWorkedHours(checkIn, checkOut);

        assertEquals(8, totalHours);
    }

    @Test
    void totalHours_shouldBeActualHoursWhenLeavingEarly() {
        LocalDateTime checkIn = LocalDateTime.of(2026, 3, 24, 8, 0);
        LocalDateTime checkOut = LocalDateTime.of(2026, 3, 24, 13, 30);

        int totalHours = attendanceService.calculateTotalWorkedHours(checkIn, checkOut);

        assertEquals(5, totalHours);
    }

    @Test
    void synchronizeDerivedFields_shouldRecalculateWorkedAndOtHours() {
        Office office = new Office();
        office.setStandardWorkHours(9);
        office.setOtMinHours(1);

        AttendanceLog log = new AttendanceLog();
        log.setOffice(office);
        log.setLogDate(LocalDateTime.of(2026, 3, 24, 0, 0).toLocalDate());
        log.setCheckInTime(LocalDateTime.of(2026, 3, 24, 8, 0));
        log.setCheckOutTime(LocalDateTime.of(2026, 3, 24, 19, 30));
        log.setWorkedHours(0);
        log.setOtHours(0);
        log.setOtType(null);

        boolean changed = attendanceService.synchronizeDerivedFields(log);

        assertTrue(changed);
        assertEquals(9, log.getWorkedHours());
        assertEquals(0, log.getOtHours());
        assertNull(log.getOtType());
    }
}

