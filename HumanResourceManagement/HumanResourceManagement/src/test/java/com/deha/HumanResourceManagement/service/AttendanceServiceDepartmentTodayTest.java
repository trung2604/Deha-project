package com.deha.HumanResourceManagement.service;

import com.deha.HumanResourceManagement.entity.AttendanceLog;
import com.deha.HumanResourceManagement.entity.Department;
import com.deha.HumanResourceManagement.entity.Office;
import com.deha.HumanResourceManagement.entity.User;
import com.deha.HumanResourceManagement.entity.enums.Role;
import com.deha.HumanResourceManagement.exception.ForbiddenException;
import com.deha.HumanResourceManagement.repository.AttendanceLogRepository;
import com.deha.HumanResourceManagement.service.impl.AttendanceService;
import com.deha.HumanResourceManagement.config.security.AccessScopeService;
import com.deha.HumanResourceManagement.service.support.OfficePolicyService;
import com.deha.HumanResourceManagement.strategy.OtTypeResolver;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AttendanceServiceDepartmentTodayTest {

    @Test
    void departmentManager_canLoadDepartmentTodayLogs() {
        LocalDate today = LocalDate.now();

        UUID departmentId = UUID.randomUUID();
        UUID officeId = UUID.randomUUID();

        Office office = new Office();
        office.setId(officeId);
        office.setStandardWorkHours(9);

        Department department = new Department();
        department.setId(departmentId);
        department.setOffice(office);

        User actor = new User();
        actor.setId(UUID.randomUUID());
        actor.setRole(Role.MANAGER_DEPARTMENT);
        actor.setOffice(office);
        actor.setDepartment(department);

        AttendanceLog log = new AttendanceLog();
        log.setId(UUID.randomUUID());
        log.setUser(new User());
        log.setOffice(office);
        log.setLogDate(today);
        log.setCheckInTime(LocalDateTime.of(today, java.time.LocalTime.of(8, 0)));
        log.setCheckOutTime(LocalDateTime.of(today, java.time.LocalTime.of(17, 30)));
        log.setWorkedHours(null);
        log.setOtHours(null);
        log.setOtType(null);

        AttendanceLogRepository attendanceLogRepository = mock(AttendanceLogRepository.class);
        when(attendanceLogRepository.findByLogDateAndUser_Department_Id(today, departmentId))
                .thenReturn(List.of(log));
        when(attendanceLogRepository.save(any(AttendanceLog.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        AccessScopeService accessScopeService = new AccessScopeService(null) {
            @Override
            public User currentUserOrThrow() {
                return actor;
            }
        };

        AttendanceService attendanceService = new AttendanceService(
                attendanceLogRepository,
                null,
                new OtTypeResolver(List.of()),
                accessScopeService,
                new OfficePolicyService()
        );

        List<AttendanceLog> result = attendanceService.getDepartmentTodayLogsOrEmpty(actor);

        assertEquals(1, result.size());
        verify(attendanceLogRepository).findByLogDateAndUser_Department_Id(today, departmentId);
        verify(attendanceLogRepository, atLeastOnce()).save(any(AttendanceLog.class));
    }

    @Test
    void officeManager_shouldBeForbiddenFromDepartmentTodayLogs() {
        Office office = new Office();
        office.setId(UUID.randomUUID());

        User actor = new User();
        actor.setId(UUID.randomUUID());
        actor.setRole(Role.MANAGER_OFFICE);
        actor.setOffice(office);
        actor.setDepartment(null);

        AttendanceLogRepository attendanceLogRepository = mock(AttendanceLogRepository.class);

        AccessScopeService accessScopeService = new AccessScopeService(null) {
            @Override
            public User currentUserOrThrow() {
                return actor;
            }
        };

        AttendanceService attendanceService = new AttendanceService(
                attendanceLogRepository,
                null,
                new OtTypeResolver(List.of()),
                accessScopeService,
                new OfficePolicyService()
        );

        assertThrows(ForbiddenException.class, () -> attendanceService.getDepartmentTodayLogsOrEmpty(actor));
        verifyNoInteractions(attendanceLogRepository);
    }
}

