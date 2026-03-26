package com.deha.HumanResourceManagement.service;

import com.deha.HumanResourceManagement.dto.ot.OtDecisionRequest;
import com.deha.HumanResourceManagement.dto.ot.OtReportCreateRequest;
import com.deha.HumanResourceManagement.dto.ot.OtReportResponse;
import com.deha.HumanResourceManagement.entity.AttendanceLog;
import com.deha.HumanResourceManagement.entity.OtReport;
import com.deha.HumanResourceManagement.entity.OtRequest;
import com.deha.HumanResourceManagement.entity.User;
import com.deha.HumanResourceManagement.entity.enums.OtReportStatus;
import com.deha.HumanResourceManagement.entity.enums.OtRequestStatus;
import com.deha.HumanResourceManagement.exception.BadRequestException;
import com.deha.HumanResourceManagement.exception.ForbiddenException;
import com.deha.HumanResourceManagement.exception.ResourceNotFoundException;
import com.deha.HumanResourceManagement.repository.AttendanceLogRepository;
import com.deha.HumanResourceManagement.repository.OtReportRepository;
import com.deha.HumanResourceManagement.repository.OtRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static java.util.Comparator.comparing;

@Service
public class OtReportService {
    private static final java.time.LocalTime OT_START_TIME = java.time.LocalTime.of(18, 0);
    private final OtReportRepository otReportRepository;
    private final OtRequestRepository otRequestRepository;
    private final AttendanceLogRepository attendanceLogRepository;
    private final AccessScopeService accessScopeService;

    public OtReportService(
            OtReportRepository otReportRepository,
            OtRequestRepository otRequestRepository,
            AttendanceLogRepository attendanceLogRepository,
            AccessScopeService accessScopeService
    ) {
        this.otReportRepository = otReportRepository;
        this.otRequestRepository = otRequestRepository;
        this.attendanceLogRepository = attendanceLogRepository;
        this.accessScopeService = accessScopeService;
    }

    @Transactional
    public OtReportResponse create(OtReportCreateRequest request) {
        User actor = accessScopeService.currentUserOrThrow();
        AttendanceLog attendanceLog = attendanceLogRepository.findById(request.getAttendanceLogId())
                .orElseThrow(() -> new ResourceNotFoundException("Attendance log not found"));
        if (attendanceLog.getUser() == null || !attendanceLog.getUser().getId().equals(actor.getId())) {
            throw new BadRequestException("You can only submit OT report for your own attendance");
        }
        if (otReportRepository.findByAttendanceLog_Id(attendanceLog.getId()).isPresent()) {
            throw new BadRequestException("OT report already submitted for this attendance");
        }

        OtRequest approvedRequest = otRequestRepository
                .findByUserAndLogDateAndStatus(actor, attendanceLog.getLogDate(), OtRequestStatus.APPROVED)
                .orElseThrow(() -> new BadRequestException("Approved OT request is required before OT report"));

        if (attendanceLog.getCheckOutTime() == null) {
            throw new BadRequestException("Cannot submit OT report before checkout");
        }

        // OT hours are only eligible after OT start time (18:00) until checkout.
        // If the employee checked in after 18:00, the eligible window starts from check-in time.
        int eligibleOtHours = calculateEligibleOtHoursFrom18ToCheckout(attendanceLog);
        if (request.getReportedOtHours() > eligibleOtHours) {
            throw new BadRequestException("Reported OT hours exceed eligible OT hours for this attendance");
        }

        // For now, "evidence" is only supported as text in reportNote.
        String reportNoteFinal = request.getReportNote() != null ? request.getReportNote().trim() : "";
        if (reportNoteFinal.isBlank()) {
            throw new BadRequestException("Report note (evidence) is required");
        }

        OtReport report = new OtReport();
        report.setAttendanceLog(attendanceLog);
        report.setOtRequest(approvedRequest);
        report.setReportedOtHours(request.getReportedOtHours());
        report.setReportNote(reportNoteFinal);
        // Approval stage depends on creator role:
        // - Department manager creating OT report should skip department-stage and go directly to office-stage.
        // - Office manager creating OT can self-approve at office-stage.
        // - Everyone else starts at department-stage.
        boolean skipDepartmentStage = accessScopeService.isDepartmentManager(actor)
                || accessScopeService.isOfficeManager(actor)
                || accessScopeService.isAdmin(actor);
        report.setStatus(skipDepartmentStage ? OtReportStatus.PENDING_OFFICE : OtReportStatus.PENDING_DEPARTMENT);
        otReportRepository.save(report);
        return OtReportResponse.fromEntity(report);
    }

    private int calculateTotalWorkedHours(AttendanceLog attendanceLog) {
        if (attendanceLog.getCheckInTime() == null || attendanceLog.getCheckOutTime() == null) return 0;
        long minutes = java.time.Duration.between(attendanceLog.getCheckInTime(), attendanceLog.getCheckOutTime()).toMinutes();
        if (minutes <= 0) return 0;
        return (int) (minutes / 60);
    }

    private int calculateEligibleOtHoursFrom18ToCheckout(AttendanceLog attendanceLog) {
        if (attendanceLog == null || attendanceLog.getCheckInTime() == null || attendanceLog.getCheckOutTime() == null) {
            return 0;
        }

        LocalDateTime checkIn = attendanceLog.getCheckInTime();
        LocalDateTime checkOut = attendanceLog.getCheckOutTime();

        LocalDateTime otStart = attendanceLog.getLogDate().atTime(OT_START_TIME);
        LocalDateTime effectiveStart = checkIn.isAfter(otStart) ? checkIn : otStart;

        long minutes = java.time.Duration.between(effectiveStart, checkOut).toMinutes();
        if (minutes <= 0) return 0;
        return (int) (minutes / 60);
    }

    @Transactional
    public OtReportResponse decide(UUID id, OtDecisionRequest request) {
        OtReport report = otReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OT report not found"));

        User manager = accessScopeService.currentUserOrThrow();
        boolean approved = Boolean.TRUE.equals(request.getApproved());

        // Self-approval rules:
        // - Office manager is allowed to self-approve their own OT report.
        // - Everyone else cannot approve their own OT report.
        if (report.getAttendanceLog() != null
                && report.getAttendanceLog().getUser() != null
                && report.getAttendanceLog().getUser().getId() != null
                && report.getAttendanceLog().getUser().getId().equals(manager.getId())
                && !accessScopeService.isOfficeManager(manager)
                && !accessScopeService.isAdmin(manager)) {
            throw new ForbiddenException("You cannot approve your own OT report");
        }

        OtReportStatus status = report.getStatus();
        if (status == OtReportStatus.APPROVED || status == OtReportStatus.REJECTED) {
            throw new BadRequestException("Only pending OT report can be decided");
        }

        if (accessScopeService.isAdmin(manager)) {
            if (!(status == OtReportStatus.PENDING
                    || status == OtReportStatus.PENDING_DEPARTMENT
                    || status == OtReportStatus.PENDING_OFFICE)) {
                throw new BadRequestException("Only pending OT report can be decided");
            }
            report.setStatus(approved ? OtReportStatus.APPROVED : OtReportStatus.REJECTED);
        } else if (accessScopeService.isDepartmentManager(manager)) {
            UUID departmentId = report.getAttendanceLog() != null
                    && report.getAttendanceLog().getUser() != null
                    && report.getAttendanceLog().getUser().getDepartment() != null
                    ? report.getAttendanceLog().getUser().getDepartment().getId()
                    : null;
            accessScopeService.assertCanManageDepartment(departmentId);

            if (!(status == OtReportStatus.PENDING || status == OtReportStatus.PENDING_DEPARTMENT)) {
                throw new BadRequestException("Department manager can only decide PENDING OT reports");
            }
            report.setStatus(approved ? OtReportStatus.PENDING_OFFICE : OtReportStatus.REJECTED);
        } else if (accessScopeService.isOfficeManager(manager)) {
            UUID officeId = report.getAttendanceLog() != null && report.getAttendanceLog().getOffice() != null
                    ? report.getAttendanceLog().getOffice().getId()
                    : null;
            accessScopeService.assertCanManageOffice(officeId);

            if (status != OtReportStatus.PENDING_OFFICE) {
                throw new BadRequestException("Office manager can only decide PENDING_OFFICE OT reports");
            }
            report.setStatus(approved ? OtReportStatus.APPROVED : OtReportStatus.REJECTED);
        } else {
            throw new ForbiddenException("You do not have permission to decide OT reports");
        }

        report.setApprovedBy(manager);
        report.setApprovedAt(LocalDateTime.now());
        report.setDecisionNote(request.getDecisionNote());
        otReportRepository.save(report);
        return OtReportResponse.fromEntity(report);
    }

    @Transactional(readOnly = true)
    public List<OtReportResponse> listMy() {
        User actor = accessScopeService.currentUserOrThrow();
        return otReportRepository.findByAttendanceLog_User_IdOrderByAttendanceLog_LogDateDesc(actor.getId())
                .stream()
                .map(OtReportResponse::fromEntity)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OtReportResponse> listPendingInMyOffice() {
        User actor = accessScopeService.currentUserOrThrow();
        if (accessScopeService.isDepartmentManager(actor)) {
            UUID departmentId = actor.getDepartment() != null ? actor.getDepartment().getId() : null;
            accessScopeService.assertCanManageDepartment(departmentId);
            List<OtReport> rows = new ArrayList<>();
            rows.addAll(otReportRepository.findByAttendanceLog_User_Department_IdAndStatusOrderByAttendanceLog_LogDateDesc(
                    departmentId,
                    OtReportStatus.PENDING_DEPARTMENT
            ));
            // Backward compatibility: legacy status PENDING behaves like PENDING_DEPARTMENT.
            rows.addAll(otReportRepository.findByAttendanceLog_User_Department_IdAndStatusOrderByAttendanceLog_LogDateDesc(
                    departmentId,
                    OtReportStatus.PENDING
            ));
            // Department manager can "track" both stages in their department.
            rows.addAll(otReportRepository.findByAttendanceLog_User_Department_IdAndStatusOrderByAttendanceLog_LogDateDesc(
                    departmentId,
                    OtReportStatus.PENDING_OFFICE
            ));
            rows.sort(comparing((OtReport r) -> r.getAttendanceLog().getLogDate()).reversed());
            return rows.stream().map(OtReportResponse::fromEntity).toList();
        }

        if (accessScopeService.isOfficeManager(actor)) {
            UUID officeId = actor.getOffice() != null ? actor.getOffice().getId() : null;
            accessScopeService.assertCanManageOffice(officeId);
            return otReportRepository.findByAttendanceLog_Office_IdAndStatusOrderByAttendanceLog_LogDateDesc(
                            officeId,
                            OtReportStatus.PENDING_OFFICE
                    )
                    .stream()
                    .map(OtReportResponse::fromEntity)
                    .toList();
        }

        // Admin: show all pending queues (both stages).
        List<OtReport> rows = new ArrayList<>();
        rows.addAll(otReportRepository.findByStatusOrderByAttendanceLog_LogDateDesc(OtReportStatus.PENDING_DEPARTMENT));
        rows.addAll(otReportRepository.findByStatusOrderByAttendanceLog_LogDateDesc(OtReportStatus.PENDING));
        rows.addAll(otReportRepository.findByStatusOrderByAttendanceLog_LogDateDesc(OtReportStatus.PENDING_OFFICE));
        rows.sort(comparing((OtReport r) -> r.getAttendanceLog().getLogDate()).reversed());
        return rows.stream().map(OtReportResponse::fromEntity).toList();
    }
}

