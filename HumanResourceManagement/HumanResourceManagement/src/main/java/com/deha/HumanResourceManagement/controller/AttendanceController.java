package com.deha.HumanResourceManagement.controller;

import com.deha.HumanResourceManagement.dto.ApiResponse;
import com.deha.HumanResourceManagement.dto.attendance.AttendanceLogResponse;
import com.deha.HumanResourceManagement.entity.User;
import com.deha.HumanResourceManagement.service.AccessScopeService;
import com.deha.HumanResourceManagement.service.AttendanceService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController extends ApiControllerSupport {
    private final AttendanceService attendanceService;
    private final AccessScopeService accessScopeService;

    public AttendanceController(AttendanceService attendanceService, AccessScopeService accessScopeService) {
        this.attendanceService = attendanceService;
        this.accessScopeService = accessScopeService;
    }

    @PostMapping("/check-in")
    public ApiResponse checkIn(HttpServletRequest request) {
        User actor = accessScopeService.currentUserOrThrow();
        List<String> clientIps = extractClientIps(request);
        attendanceService.checkIn(actor, clientIps);
        return success("Checked in successfully", HttpStatus.OK, null);
    }

    @PostMapping("/check-out")
    public ApiResponse checkOut(HttpServletRequest request) {
        User actor = accessScopeService.currentUserOrThrow();
        List<String> clientIps = extractClientIps(request);
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

    private List<String> extractClientIps(HttpServletRequest request) {
        Set<String> unique = new LinkedHashSet<>();

        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            for (String part : forwardedFor.split(",")) {
                String value = part == null ? "" : part.trim();
                if (!value.isBlank()) unique.add(value);
            }
        }

        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            unique.add(realIp.trim());
        }

        String remoteAddr = request.getRemoteAddr();
        if (remoteAddr != null && !remoteAddr.isBlank()) {
            unique.add(remoteAddr.trim());
        }

        return new ArrayList<>(unique);
    }

    
}

