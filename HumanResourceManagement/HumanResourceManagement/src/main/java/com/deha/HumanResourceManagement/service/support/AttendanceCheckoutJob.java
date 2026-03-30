package com.deha.HumanResourceManagement.service.support;

import com.deha.HumanResourceManagement.service.IAttendanceService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class AttendanceCheckoutJob {
    private final IAttendanceService attendanceService;

    public AttendanceCheckoutJob(IAttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @Scheduled(cron = "0 */5 * * * *")
    public void run() {
        attendanceService.autoCheckout(LocalDate.now());
    }
}


