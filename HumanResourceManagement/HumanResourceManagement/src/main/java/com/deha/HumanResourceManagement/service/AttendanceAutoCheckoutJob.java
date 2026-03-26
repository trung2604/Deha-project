package com.deha.HumanResourceManagement.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class AttendanceAutoCheckoutJob {
    private final AttendanceService attendanceService;

    public AttendanceAutoCheckoutJob(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @Scheduled(cron = "0 0 18 * * *")
    public void autoCheckoutAt18() {
        attendanceService.autoCheckoutOpenLogs(LocalDate.now());
    }
}

