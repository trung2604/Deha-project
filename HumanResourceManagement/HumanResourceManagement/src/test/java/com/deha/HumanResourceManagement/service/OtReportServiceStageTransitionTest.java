package com.deha.HumanResourceManagement.service;

import com.deha.HumanResourceManagement.dto.ot.OtDecisionRequest;
import com.deha.HumanResourceManagement.dto.ot.OtReportResponse;
import com.deha.HumanResourceManagement.entity.AttendanceLog;
import com.deha.HumanResourceManagement.entity.Department;
import com.deha.HumanResourceManagement.entity.Office;
import com.deha.HumanResourceManagement.entity.OtReport;
import com.deha.HumanResourceManagement.entity.OtRequest;
import com.deha.HumanResourceManagement.entity.User;
import com.deha.HumanResourceManagement.entity.enums.OtReportStatus;
import com.deha.HumanResourceManagement.entity.enums.Role;
import com.deha.HumanResourceManagement.exception.BadRequestException;
import com.deha.HumanResourceManagement.exception.ForbiddenException;
import com.deha.HumanResourceManagement.repository.AttendanceLogRepository;
import com.deha.HumanResourceManagement.repository.OtReportRepository;
import com.deha.HumanResourceManagement.repository.OtRequestRepository;
import com.deha.HumanResourceManagement.repository.OtSessionRepository;
import com.deha.HumanResourceManagement.service.impl.OtReportService;
import com.deha.HumanResourceManagement.config.security.AccessScopeService;
import com.deha.HumanResourceManagement.service.support.OfficePolicyService;
import com.deha.HumanResourceManagement.service.ot.workflow.OtReportWorkflowService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OtReportServiceStageTransitionTest {

    @Test
    void decide_withoutExpectedVersion_shouldThrowBadRequest() {
        UUID reportId = UUID.randomUUID();

        Office office = new Office();
        office.setId(UUID.randomUUID());

        Department department = new Department();
        department.setId(UUID.randomUUID());
        department.setOffice(office);

        User reportCreator = new User();
        reportCreator.setId(UUID.randomUUID());
        reportCreator.setRole(Role.EMPLOYEE);
        reportCreator.setOffice(office);
        reportCreator.setDepartment(department);

        AttendanceLog attendanceLog = new AttendanceLog();
        attendanceLog.setId(UUID.randomUUID());
        attendanceLog.setUser(reportCreator);
        attendanceLog.setOffice(office);
        attendanceLog.setLogDate(LocalDate.now());

        OtReport report = new OtReport();
        report.setId(reportId);
        report.setVersion(2L);
        report.setAttendanceLog(attendanceLog);
        report.setStatus(OtReportStatus.PENDING_DEPARTMENT);

        User departmentManager = new User();
        departmentManager.setId(UUID.randomUUID());
        departmentManager.setRole(Role.MANAGER_DEPARTMENT);
        departmentManager.setOffice(office);
        departmentManager.setDepartment(department);

        OtReportRepository otReportRepository = mock(OtReportRepository.class);
        when(otReportRepository.findById(reportId)).thenReturn(Optional.of(report));

        AccessScopeService accessScopeService = new AccessScopeService(null) {
            @Override
            public User currentUserOrThrow() {
                return departmentManager;
            }
        };

        OtReportService service = new OtReportService(
                otReportRepository,
                mock(OtRequestRepository.class),
                mock(AttendanceLogRepository.class),
                mock(OtSessionRepository.class),
                accessScopeService,
                new OfficePolicyService(),
                new OtReportWorkflowService()
        );

        OtDecisionRequest decision = new OtDecisionRequest();
        decision.setApproved(true);

        assertThrows(BadRequestException.class, () -> service.decide(reportId, decision));
        verify(otReportRepository, never()).save(any(OtReport.class));
    }

    @Test
    void decide_withStaleExpectedVersion_shouldProceedWithDetachedMerge() {
        UUID reportId = UUID.randomUUID();

        Office office = new Office();
        office.setId(UUID.randomUUID());

        Department department = new Department();
        department.setId(UUID.randomUUID());
        department.setOffice(office);

        User reportCreator = new User();
        reportCreator.setId(UUID.randomUUID());
        reportCreator.setRole(Role.EMPLOYEE);
        reportCreator.setOffice(office);
        reportCreator.setDepartment(department);

        AttendanceLog attendanceLog = new AttendanceLog();
        attendanceLog.setId(UUID.randomUUID());
        attendanceLog.setUser(reportCreator);
        attendanceLog.setOffice(office);
        attendanceLog.setLogDate(LocalDate.now());

        OtReport report = new OtReport();
        report.setId(reportId);
        report.setVersion(7L);
        report.setAttendanceLog(attendanceLog);
        report.setStatus(OtReportStatus.PENDING_DEPARTMENT);

        User departmentManager = new User();
        departmentManager.setId(UUID.randomUUID());
        departmentManager.setRole(Role.MANAGER_DEPARTMENT);
        departmentManager.setOffice(office);
        departmentManager.setDepartment(department);

        OtReportRepository otReportRepository = mock(OtReportRepository.class);
        when(otReportRepository.findById(reportId)).thenReturn(Optional.of(report));
        when(otReportRepository.saveAndFlush(any(OtReport.class))).thenAnswer(inv -> inv.getArgument(0));

        AccessScopeService accessScopeService = new AccessScopeService(null) {
            @Override
            public User currentUserOrThrow() {
                return departmentManager;
            }
        };

        OtReportService service = new OtReportService(
                otReportRepository,
                mock(OtRequestRepository.class),
                mock(AttendanceLogRepository.class),
                mock(OtSessionRepository.class),
                accessScopeService,
                new OfficePolicyService(),
                new OtReportWorkflowService()
        );

        OtDecisionRequest decision = new OtDecisionRequest();
        decision.setApproved(true);
        decision.setExpectedVersion(6L);
        decision.setExpectedVersion(6L);

        OtReportResponse response = service.decide(reportId, decision);
        assertNotNull(response);
        assertEquals(OtReportStatus.PENDING_OFFICE, response.getStatus());
    }

    @Test
    void departmentManager_approvesPendingDepartmentReport_toPendingOfficeReport() {
        UUID officeId = UUID.randomUUID();
        UUID departmentId = UUID.randomUUID();
        UUID attendanceLogId = UUID.randomUUID();
        UUID reportId = UUID.randomUUID();

        Office office = new Office();
        office.setId(officeId);

        Department department = new Department();
        department.setId(departmentId);
        department.setOffice(office);

        User reportCreator = new User();
        reportCreator.setId(UUID.randomUUID());
        reportCreator.setRole(Role.EMPLOYEE);
        reportCreator.setOffice(office);
        reportCreator.setDepartment(department);

        AttendanceLog attendanceLog = new AttendanceLog();
        attendanceLog.setId(attendanceLogId);
        attendanceLog.setUser(reportCreator);
        attendanceLog.setOffice(office);
        attendanceLog.setCheckInTime(LocalDateTime.now().minusHours(2));
        attendanceLog.setCheckOutTime(LocalDateTime.now().minusHours(1));
        attendanceLog.setLogDate(LocalDate.now());

        OtRequest approvedRequest = new OtRequest();
        approvedRequest.setStatus(null); // not used in decide()

        OtReport report = new OtReport();
        report.setId(reportId);
        report.setVersion(1L);
        report.setAttendanceLog(attendanceLog);
        report.setOtRequest(approvedRequest);
        report.setReportedOtHours(1);
        report.setReportNote("note");
        report.setStatus(OtReportStatus.PENDING_DEPARTMENT);

        User departmentManager = new User();
        departmentManager.setId(UUID.randomUUID());
        departmentManager.setRole(Role.MANAGER_DEPARTMENT);
        departmentManager.setOffice(office);
        departmentManager.setDepartment(department);

        OtReportRepository otReportRepository = mock(OtReportRepository.class);
        when(otReportRepository.findById(reportId)).thenReturn(Optional.of(report));
        when(otReportRepository.saveAndFlush(any(OtReport.class))).thenAnswer(inv -> inv.getArgument(0));

        AccessScopeService accessScopeService = new AccessScopeService(null) {
            @Override
            public User currentUserOrThrow() {
                return departmentManager;
            }
        };

        OtReportService service = new OtReportService(
                otReportRepository,
                mock(OtRequestRepository.class),
                mock(AttendanceLogRepository.class),
                mock(OtSessionRepository.class),
                accessScopeService,
                new OfficePolicyService(),
                new OtReportWorkflowService()
        );

        OtDecisionRequest decision = new OtDecisionRequest();
        decision.setApproved(true);
        decision.setDecisionNote("ok");
        decision.setExpectedVersion(1L);

        OtReportResponse response = service.decide(reportId, decision);

        assertEquals(OtReportStatus.PENDING_OFFICE, response.getStatus());
        assertEquals(departmentManager.getId(), response.getApprovedById());
    }

    @Test
    void officeManager_approvesPendingOfficeReport_toApproved() {
        UUID officeId = UUID.randomUUID();
        UUID reportId = UUID.randomUUID();

        Office office = new Office();
        office.setId(officeId);

        Department department = new Department();
        department.setId(UUID.randomUUID());
        department.setOffice(office);

        User reportCreator = new User();
        reportCreator.setId(UUID.randomUUID());
        reportCreator.setRole(Role.EMPLOYEE);
        reportCreator.setOffice(office);
        reportCreator.setDepartment(department);

        AttendanceLog attendanceLog = new AttendanceLog();
        attendanceLog.setId(UUID.randomUUID());
        attendanceLog.setUser(reportCreator);
        attendanceLog.setOffice(office);
        attendanceLog.setLogDate(LocalDate.now());

        OtReport report = new OtReport();
        report.setId(reportId);
        report.setVersion(1L);
        report.setAttendanceLog(attendanceLog);
        report.setStatus(OtReportStatus.PENDING_OFFICE);

        User officeManager = new User();
        officeManager.setId(UUID.randomUUID());
        officeManager.setRole(Role.MANAGER_OFFICE);
        officeManager.setOffice(office);

        OtReportRepository otReportRepository = mock(OtReportRepository.class);
        when(otReportRepository.findById(reportId)).thenReturn(Optional.of(report));
        when(otReportRepository.saveAndFlush(any(OtReport.class))).thenAnswer(inv -> inv.getArgument(0));

        AccessScopeService accessScopeService = new AccessScopeService(null) {
            @Override
            public User currentUserOrThrow() {
                return officeManager;
            }
        };

        OtReportService service = new OtReportService(
                otReportRepository,
                mock(OtRequestRepository.class),
                mock(AttendanceLogRepository.class),
                mock(OtSessionRepository.class),
                accessScopeService,
                new OfficePolicyService(),
                new OtReportWorkflowService()
        );

        OtDecisionRequest decision = new OtDecisionRequest();
        decision.setApproved(true);
        decision.setDecisionNote("ok");
        decision.setExpectedVersion(1L);

        OtReportResponse response = service.decide(reportId, decision);
        assertEquals(OtReportStatus.APPROVED, response.getStatus());
        assertEquals(officeManager.getId(), response.getApprovedById());
    }

    @Test
    void departmentManager_scopeMismatch_shouldThrowForbidden() {
        UUID officeId = UUID.randomUUID();
        UUID correctDepartmentId = UUID.randomUUID();
        UUID wrongDepartmentId = UUID.randomUUID();
        UUID reportId = UUID.randomUUID();

        Office office = new Office();
        office.setId(officeId);

        Department correctDept = new Department();
        correctDept.setId(correctDepartmentId);
        correctDept.setOffice(office);

        Department wrongDept = new Department();
        wrongDept.setId(wrongDepartmentId);
        wrongDept.setOffice(office);

        User reportCreator = new User();
        reportCreator.setId(UUID.randomUUID());
        reportCreator.setRole(Role.EMPLOYEE);
        reportCreator.setOffice(office);
        reportCreator.setDepartment(correctDept);

        AttendanceLog attendanceLog = new AttendanceLog();
        attendanceLog.setId(UUID.randomUUID());
        attendanceLog.setUser(reportCreator);
        attendanceLog.setOffice(office);
        attendanceLog.setLogDate(LocalDate.now());

        OtReport report = new OtReport();
        report.setId(reportId);
        report.setVersion(1L);
        report.setAttendanceLog(attendanceLog);
        report.setStatus(OtReportStatus.PENDING_DEPARTMENT);

        User departmentManager = new User();
        departmentManager.setId(UUID.randomUUID());
        departmentManager.setRole(Role.MANAGER_DEPARTMENT);
        departmentManager.setOffice(office);
        departmentManager.setDepartment(wrongDept);

        OtReportRepository otReportRepository = mock(OtReportRepository.class);
        when(otReportRepository.findById(reportId)).thenReturn(Optional.of(report));

        AccessScopeService accessScopeService = new AccessScopeService(null) {
            @Override
            public User currentUserOrThrow() {
                return departmentManager;
            }
        };

        OtReportService service = new OtReportService(
                otReportRepository,
                mock(OtRequestRepository.class),
                mock(AttendanceLogRepository.class),
                mock(OtSessionRepository.class),
                accessScopeService,
                new OfficePolicyService(),
                new OtReportWorkflowService()
        );

        OtDecisionRequest decision = new OtDecisionRequest();
        decision.setApproved(true);
        decision.setDecisionNote("ok");
        decision.setExpectedVersion(1L);

        Executable action = () -> service.decide(reportId, decision);
        assertThrows(ForbiddenException.class, action);
    }

    @Test
    void selfApproval_shouldThrowForbidden() {
        UUID officeId = UUID.randomUUID();
        UUID departmentId = UUID.randomUUID();
        UUID reportId = UUID.randomUUID();

        Office office = new Office();
        office.setId(officeId);

        Department department = new Department();
        department.setId(departmentId);
        department.setOffice(office);

        UUID managerId = UUID.randomUUID();

        User reportCreator = new User();
        reportCreator.setId(managerId);
        reportCreator.setRole(Role.EMPLOYEE);
        reportCreator.setOffice(office);
        reportCreator.setDepartment(department);

        AttendanceLog attendanceLog = new AttendanceLog();
        attendanceLog.setId(UUID.randomUUID());
        attendanceLog.setUser(reportCreator);
        attendanceLog.setOffice(office);
        attendanceLog.setLogDate(LocalDate.now());

        OtReport report = new OtReport();
        report.setId(reportId);
        report.setVersion(1L);
        report.setAttendanceLog(attendanceLog);
        report.setStatus(OtReportStatus.PENDING_DEPARTMENT);

        User departmentManager = new User();
        departmentManager.setId(managerId);
        departmentManager.setRole(Role.MANAGER_DEPARTMENT);
        departmentManager.setOffice(office);
        departmentManager.setDepartment(department);

        OtReportRepository otReportRepository = mock(OtReportRepository.class);
        when(otReportRepository.findById(reportId)).thenReturn(Optional.of(report));

        AccessScopeService accessScopeService = new AccessScopeService(null) {
            @Override
            public User currentUserOrThrow() {
                return departmentManager;
            }
        };

        OtReportService service = new OtReportService(
                otReportRepository,
                mock(OtRequestRepository.class),
                mock(AttendanceLogRepository.class),
                mock(OtSessionRepository.class),
                accessScopeService,
                new OfficePolicyService(),
                new OtReportWorkflowService()
        );

        OtDecisionRequest decision = new OtDecisionRequest();
        decision.setApproved(true);
        decision.setDecisionNote("ok");
        decision.setExpectedVersion(1L);

        Executable action = () -> service.decide(reportId, decision);
        assertThrows(ForbiddenException.class, action);
    }
}

