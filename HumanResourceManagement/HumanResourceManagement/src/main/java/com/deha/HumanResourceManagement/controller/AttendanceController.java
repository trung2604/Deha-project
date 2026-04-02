package com.deha.HumanResourceManagement.controller;

import com.deha.HumanResourceManagement.dto.ApiResponse;
import com.deha.HumanResourceManagement.dto.attendance.AttendanceLogResponse;
import com.deha.HumanResourceManagement.entity.User;
import com.deha.HumanResourceManagement.service.IAttendanceService;
import com.deha.HumanResourceManagement.service.support.AccessScopeService;
import com.deha.HumanResourceManagement.service.support.ClientIpResolverService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController extends ApiControllerSupport {
    private final IAttendanceService attendanceService;
    private final AccessScopeService accessScopeService;
    private final ClientIpResolverService clientIpResolverService;

    public AttendanceController(
            IAttendanceService attendanceService,
            AccessScopeService accessScopeService,
            ClientIpResolverService clientIpResolverService
    ) {
        this.attendanceService = attendanceService;
        this.accessScopeService = accessScopeService;
        this.clientIpResolverService = clientIpResolverService;
    }

    @PostMapping("/check-in")
    public ApiResponse checkIn(HttpServletRequest request) {
        User actor = accessScopeService.currentUserOrThrow();
        List<String> clientIps = clientIpResolverService.extractClientIps(request);
        attendanceService.checkIn(actor, clientIps);
        return success("Checked in successfully", HttpStatus.OK, null);
    }

    @PostMapping("/check-out")
    public ApiResponse checkOut(HttpServletRequest request) {
        User actor = accessScopeService.currentUserOrThrow();
        List<String> clientIps = clientIpResolverService.extractClientIps(request);
        attendanceService.checkOut(actor, clientIps);
        return success("Checked out successfully", HttpStatus.OK, null);
    }

    @GetMapping("/today")
    public ApiResponse today() {
        User actor = accessScopeService.currentUserOrThrow();
        return success("Today's attendance retrieved successfully", HttpStatus.OK,
                AttendanceLogResponse.fromEntity(attendanceService.getTodayLogOrNull(actor)));
    }

    @GetMapping("/department/today")
    public ApiResponse departmentToday() {
        User actor = accessScopeService.currentUserOrThrow();
        var logs = attendanceService.getDepartmentTodayLogsOrEmpty(actor);
        return success(
                "Department today's attendance retrieved successfully",
                HttpStatus.OK,
                logs.stream().map(AttendanceLogResponse::fromEntity).toList()
        );
    }



}

