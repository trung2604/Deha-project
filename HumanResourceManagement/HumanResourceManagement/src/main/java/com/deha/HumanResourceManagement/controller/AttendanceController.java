package com.deha.HumanResourceManagement.controller;

import com.deha.HumanResourceManagement.config.security.AccessScopeService;
import com.deha.HumanResourceManagement.dto.ApiResponse;
import com.deha.HumanResourceManagement.mapper.attendance.AttendanceMapper;
import com.deha.HumanResourceManagement.entity.User;
import com.deha.HumanResourceManagement.service.IAttendanceService;
import com.deha.HumanResourceManagement.service.support.ClientIpResolverService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Attendance", description = "Attendance check-in/check-out and daily attendance tracking APIs")
@RestController
@RequestMapping("/api/attendance")
public class AttendanceController extends ApiControllerSupport {
    private final IAttendanceService attendanceService;
    private final AccessScopeService accessScopeService;
    private final ClientIpResolverService clientIpResolverService;
    private final AttendanceMapper attendanceMapper;

    public AttendanceController(
            IAttendanceService attendanceService,
            AccessScopeService accessScopeService,
            ClientIpResolverService clientIpResolverService,
            AttendanceMapper attendanceMapper
    ) {
        this.attendanceService = attendanceService;
        this.accessScopeService = accessScopeService;
        this.clientIpResolverService = clientIpResolverService;
        this.attendanceMapper = attendanceMapper;
    }

    @PostMapping("/check-in")
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER_DEPARTMENT','MANAGER_OFFICE')")
    public ApiResponse checkIn(HttpServletRequest request) {
        User actor = accessScopeService.currentUserOrThrow();
        String clientIps = clientIpResolverService.extractClientIp(request);
        attendanceService.checkIn(actor, clientIps);
        return success("Checked in successfully", HttpStatus.OK, null);
    }

    @PostMapping("/check-out")
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER_DEPARTMENT','MANAGER_OFFICE')")
    public ApiResponse checkOut(HttpServletRequest request) {
        User actor = accessScopeService.currentUserOrThrow();
        String clientIps = clientIpResolverService.extractClientIp(request);
        attendanceService.checkOut(actor, clientIps);
        return success("Checked out successfully", HttpStatus.OK, null);
    }

    @GetMapping("/today")
    @PreAuthorize("hasAnyRole('EMPLOYEE','MANAGER_DEPARTMENT','MANAGER_OFFICE','ADMIN')")
    public ApiResponse today() {
        User actor = accessScopeService.currentUserOrThrow();
        return success("Today's attendance retrieved successfully", HttpStatus.OK,
                attendanceMapper.toResponse(attendanceService.getTodayLogOrNull(actor)));
    }

    @GetMapping("/department/today")
    @PreAuthorize("hasRole('MANAGER_DEPARTMENT')")
    public ApiResponse departmentToday() {
        User actor = accessScopeService.currentUserOrThrow();
        var logs = attendanceService.getDepartmentTodayLogsOrEmpty(actor);
        return success(
                "Department today's attendance retrieved successfully",
                HttpStatus.OK,
                logs.stream().map(attendanceMapper::toResponse).toList()
        );
    }

    @GetMapping("/office/today")
    @PreAuthorize("hasAnyRole('MANAGER_OFFICE','ADMIN')")
    public ApiResponse officeToday(@RequestParam(required = false) UUID officeId) {
        User actor = accessScopeService.currentUserOrThrow();
        var logs = attendanceService.getOfficeTodayLogsOrEmpty(actor, officeId);
        return success(
                "Office today's attendance retrieved successfully",
                HttpStatus.OK,
                logs.stream().map(attendanceMapper::toResponse).toList()
        );
    }
}

