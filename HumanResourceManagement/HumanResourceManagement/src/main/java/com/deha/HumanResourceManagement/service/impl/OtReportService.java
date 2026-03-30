package com.deha.HumanResourceManagement.service.impl;

import com.deha.HumanResourceManagement.dto.ot.OtDecisionRequest;
import com.deha.HumanResourceManagement.dto.ot.OtReportCreateRequest;
import com.deha.HumanResourceManagement.dto.ot.OtReportResponse;
import com.deha.HumanResourceManagement.entity.AttendanceLog;
import com.deha.HumanResourceManagement.entity.OtReport;
import com.deha.HumanResourceManagement.entity.OtRequest;
import com.deha.HumanResourceManagement.entity.OtSession;
import com.deha.HumanResourceManagement.entity.User;
import com.deha.HumanResourceManagement.entity.enums.OtReportStatus;
import com.deha.HumanResourceManagement.entity.enums.OtRequestStatus;
import com.deha.HumanResourceManagement.exception.BadRequestException;
import com.deha.HumanResourceManagement.exception.ForbiddenException;
import com.deha.HumanResourceManagement.exception.ResourceNotFoundException;
import com.deha.HumanResourceManagement.repository.AttendanceLogRepository;
import com.deha.HumanResourceManagement.repository.OtReportRepository;
import com.deha.HumanResourceManagement.repository.OtRequestRepository;
import com.deha.HumanResourceManagement.repository.OtSessionRepository;
import com.deha.HumanResourceManagement.service.IOtReportService;
import com.deha.HumanResourceManagement.service.support.AccessScopeService;
import com.deha.HumanResourceManagement.service.support.OfficePolicyService;
import com.deha.HumanResourceManagement.service.ot.workflow.OtReportWorkflowService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.Comparator.comparing;

@Service
public class OtReportService implements IOtReportService {
    private final OtReportRepository otReportRepository;
    private final OtRequestRepository otRequestRepository;
    private final AttendanceLogRepository attendanceLogRepository;
    private final OtSessionRepository otSessionRepository;
    private final AccessScopeService accessScopeService;
    private final OfficePolicyService officePolicyService;
    private final OtReportWorkflowService otReportWorkflowService;

    public OtReportService(
            OtReportRepository otReportRepository,
            OtRequestRepository otRequestRepository,
            AttendanceLogRepository attendanceLogRepository,
            OtSessionRepository otSessionRepository,
            AccessScopeService accessScopeService,
            OfficePolicyService officePolicyService,
            OtReportWorkflowService otReportWorkflowService
    ) {
        this.otReportRepository = otReportRepository;
        this.otRequestRepository = otRequestRepository;
        this.attendanceLogRepository = attendanceLogRepository;
        this.otSessionRepository = otSessionRepository;
        this.accessScopeService = accessScopeService;
        this.officePolicyService = officePolicyService;
        this.otReportWorkflowService = otReportWorkflowService;
    }

    @Override
    @Transactional
    public OtReportResponse create(OtReportCreateRequest request) {
        User actor = accessScopeService.currentUserOrThrow();
        boolean canCreateOt = accessScopeService.isEmployee(actor)
                || accessScopeService.isDepartmentManager(actor);
        if (!canCreateOt) {
            throw new ForbiddenException("You do not have permission to submit OT report");
        }
        OtSession otSession = otSessionRepository.findById(request.getOtSessionId())
                .orElseThrow(() -> new ResourceNotFoundException("OT session not found"));

        if (otSession.getUser() == null || !otSession.getUser().getId().equals(actor.getId())) {
            throw new BadRequestException("You can only submit OT report for your own OT session");
        }
        if (otReportRepository.findByOtSession_Id(otSession.getId()).isPresent()) {
            throw new BadRequestException("OT report already submitted for this OT session");
        }

        OtRequest approvedRequest = otRequestRepository
                .findByUserAndLogDateAndStatus(actor, otSession.getLogDate(), OtRequestStatus.APPROVED)
                .orElseThrow(() -> new BadRequestException("Approved OT request is required before OT report"));

        if (otSession.getCheckOutTime() == null) {
            throw new BadRequestException("Cannot submit OT report before OT checkout");
        }

        AttendanceLog attendanceLog = attendanceLogRepository.findByUserAndLogDate(actor, otSession.getLogDate())
                .orElseThrow(() -> new BadRequestException("Attendance log is required for OT report"));

        int eligibleOtHours = calculateEligibleOtHoursByOtSession(otSession);
        int minimumOtHours = officePolicyService.otMinHours(attendanceLog.getOffice());
        if (eligibleOtHours < minimumOtHours) {
            throw new BadRequestException("Eligible OT hours are below office minimum OT hours");
        }
        if (request.getReportedOtHours() > eligibleOtHours) {
            throw new BadRequestException("Reported OT hours exceed eligible OT hours for this attendance");
        }

        String reportNoteFinal = request.getReportNote() != null ? request.getReportNote().trim() : "";
        if (reportNoteFinal.isBlank()) {
            throw new BadRequestException("Report note (evidence) is required");
        }

        OtReport report = new OtReport();
        report.setAttendanceLog(attendanceLog);
        report.setOtRequest(approvedRequest);
        report.setOtSession(otSession);
        report.setReportedOtHours(request.getReportedOtHours());
        report.setReportNote(reportNoteFinal);

        report.setStatus(otReportWorkflowService.initialStatus(actor.getRole()));
        otReportRepository.save(report);
        return OtReportResponse.fromEntity(report);
    }

    private int calculateEligibleOtHoursByOtSession(OtSession otSession) {
        if (otSession == null
                || otSession.getCheckInTime() == null
                || otSession.getCheckOutTime() == null) {
            return 0;
        }
        long minutes = java.time.Duration.between(otSession.getCheckInTime(), otSession.getCheckOutTime()).toMinutes();
        if (minutes <= 0) return 0;
        return (int) (minutes / 60);
    }

    @Override
    @Transactional
    public OtReportResponse decide(UUID id, OtDecisionRequest request) {
        OtReport report = otReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OT report not found"));

        User manager = accessScopeService.currentUserOrThrow();
        boolean approved = Boolean.TRUE.equals(request.getApproved());

        boolean isSelfReport = report.getAttendanceLog() != null
                && report.getAttendanceLog().getUser() != null
                && report.getAttendanceLog().getUser().getId() != null
                && report.getAttendanceLog().getUser().getId().equals(manager.getId());

        if (isSelfReport && !accessScopeService.isOfficeManager(manager)) {
            throw new ForbiddenException("You cannot approve your own OT report");
        }

        if (accessScopeService.isDepartmentManager(manager)) {
            UUID departmentId = report.getAttendanceLog() != null
                    && report.getAttendanceLog().getUser() != null
                    && report.getAttendanceLog().getUser().getDepartment() != null
                    ? report.getAttendanceLog().getUser().getDepartment().getId()
                    : null;
            accessScopeService.assertCanManageDepartment(departmentId);
        } else if (accessScopeService.isOfficeManager(manager)) {
            UUID officeId = report.getAttendanceLog() != null
                    && report.getAttendanceLog().getOffice() != null
                    ? report.getAttendanceLog().getOffice().getId()
                    : null;
            accessScopeService.assertCanManageOffice(officeId);
        } else {
            throw new ForbiddenException("You do not have permission to decide OT reports");
        }

        report.setStatus(otReportWorkflowService.nextStatus(manager.getRole(), report.getStatus(), approved));

        report.setApprovedBy(manager);
        report.setApprovedAt(LocalDateTime.now());
        report.setDecisionNote(request.getDecisionNote());
        otReportRepository.save(report);
        return OtReportResponse.fromEntity(report);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OtReportResponse> listByApprovalScope() {
        User currentApprover = accessScopeService.currentUserOrThrow();

        if (accessScopeService.isDepartmentManager(currentApprover)) {
            return otReportRepository
                    .findAllByAttendanceLog_User_Department_IdOrderByAttendanceLog_LogDateDesc(
                            currentApprover.getDepartment().getId()
                    )
                    .stream()
                    .map(OtReportResponse::fromEntity)
                    .toList();
        }
        if (accessScopeService.isOfficeManager(currentApprover)) {
            return otReportRepository
                    .findAllByAttendanceLog_User_Office_IdOrderByAttendanceLog_LogDateDesc(
                            currentApprover.getOffice().getId()
                    )
                    .stream()
                    .map(OtReportResponse::fromEntity)
                    .toList();
        }

        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OtReportResponse> listPendingForApproverScope() {
        User currentApprover = accessScopeService.currentUserOrThrow();

        if (accessScopeService.isDepartmentManager(currentApprover)) {
            UUID approverDepartmentId = currentApprover.getDepartment() != null ? currentApprover.getDepartment().getId() : null;
            accessScopeService.assertCanManageDepartment(approverDepartmentId);
            List<OtReport> pendingReports = new ArrayList<>();
            for (OtReportStatus status : otReportWorkflowService.pendingStatusesForDepartmentManager()) {
                pendingReports.addAll(otReportRepository.findByAttendanceLog_User_Department_IdAndStatusOrderByAttendanceLog_LogDateDesc(
                        approverDepartmentId,
                        status
                ));
            }
            pendingReports.sort(comparing((OtReport r) -> r.getAttendanceLog().getLogDate()).reversed());
            return pendingReports.stream().map(OtReportResponse::fromEntity).toList();
        }

        if (accessScopeService.isOfficeManager(currentApprover)) {
            UUID approverOfficeId = currentApprover.getOffice() != null ? currentApprover.getOffice().getId() : null;
            accessScopeService.assertCanManageOffice(approverOfficeId);
            List<OtReport> pendingReports = new ArrayList<>();
            for (OtReportStatus status : otReportWorkflowService.pendingStatusesForOfficeManager()) {
                pendingReports.addAll(otReportRepository.findByAttendanceLog_Office_IdAndStatusOrderByAttendanceLog_LogDateDesc(
                        approverOfficeId,
                        status
                ));
            }
            pendingReports.sort(comparing((OtReport r) -> r.getAttendanceLog().getLogDate()).reversed());
            return pendingReports.stream().map(OtReportResponse::fromEntity).toList();
        }

        throw new ForbiddenException("You do not have permission to view OT reports");
    }

    @Override
    @Transactional(readOnly = true)
    public List<OtReportResponse> listMy() {
        User actor = accessScopeService.currentUserOrThrow();
        return otReportRepository
                .findByAttendanceLog_User_IdOrderByAttendanceLog_LogDateDesc(actor.getId())
                .stream()
                .map(OtReportResponse::fromEntity)
                .toList();
    }
}


