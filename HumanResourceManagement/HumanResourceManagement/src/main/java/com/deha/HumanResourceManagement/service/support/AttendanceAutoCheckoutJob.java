package com.deha.HumanResourceManagement.service.support;

import com.deha.HumanResourceManagement.service.IAttendanceService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class AttendanceAutoCheckoutJob {
    private final IAttendanceService attendanceService;

    public AttendanceAutoCheckoutJob(IAttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @Scheduled(cron = "0 */5 * * * *")
    public void autoCheckoutByOfficePolicy() {
        attendanceService.autoCheckoutOpenLogs(LocalDate.now());
    }
}

