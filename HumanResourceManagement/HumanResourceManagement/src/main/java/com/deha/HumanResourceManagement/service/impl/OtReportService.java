package com.deha.HumanResourceManagement.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.deha.HumanResourceManagement.config.security.AccessScopeService;
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
import com.deha.HumanResourceManagement.mapper.ot.OtReportMapper;
import com.deha.HumanResourceManagement.repository.AttendanceLogRepository;
import com.deha.HumanResourceManagement.repository.OtReportRepository;
import com.deha.HumanResourceManagement.repository.OtRequestRepository;
import com.deha.HumanResourceManagement.repository.OtSessionRepository;
import com.deha.HumanResourceManagement.service.IOtReportService;
import com.deha.HumanResourceManagement.service.support.OfficePolicyService;
import com.deha.HumanResourceManagement.service.ot.workflow.OtReportWorkflowService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static java.util.Comparator.comparing;

@Service
public class OtReportService implements IOtReportService {
    private static final int REPORT_NOTE_MIN_LENGTH = 10;
    private static final int REPORT_NOTE_MAX_LENGTH = 500;
    private static final long MAX_EVIDENCE_SIZE_BYTES = 8L * 1024 * 1024;
    private static final Set<String> ALLOWED_EVIDENCE_MIME_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "application/pdf",
            "text/plain",
            "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "application/vnd.ms-excel",
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    );
    private static final Logger log = LoggerFactory.getLogger(OtReportService.class);

    private final OtReportRepository otReportRepository;
    private final OtRequestRepository otRequestRepository;
    private final AttendanceLogRepository attendanceLogRepository;
    private final OtSessionRepository otSessionRepository;
    private final AccessScopeService accessScopeService;
    private final OfficePolicyService officePolicyService;
    private final OtReportWorkflowService otReportWorkflowService;
    private final OtReportMapper otReportMapper;
    @Autowired(required = false)
    private Cloudinary cloudinary;
    @PersistenceContext
    private EntityManager entityManager;

    public OtReportService(
            OtReportRepository otReportRepository,
            OtRequestRepository otRequestRepository,
            AttendanceLogRepository attendanceLogRepository,
            OtSessionRepository otSessionRepository,
            AccessScopeService accessScopeService,
            OfficePolicyService officePolicyService,
            OtReportWorkflowService otReportWorkflowService,
            OtReportMapper otReportMapper
    ) {
        this.otReportRepository = otReportRepository;
        this.otRequestRepository = otRequestRepository;
        this.attendanceLogRepository = attendanceLogRepository;
        this.otSessionRepository = otSessionRepository;
        this.accessScopeService = accessScopeService;
        this.officePolicyService = officePolicyService;
        this.otReportWorkflowService = otReportWorkflowService;
        this.otReportMapper = otReportMapper;
    }

    @Override
    @Transactional
    public OtReportResponse create(OtReportCreateRequest request) {
        return create(request, null);
    }

    @Override
    @Transactional
    public OtReportResponse create(OtReportCreateRequest request, MultipartFile evidenceFile) {
        if (request == null || request.getOtSessionId() == null) {
            throw new BadRequestException("OT session id is required");
        }
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

        int eligibleOtHours = calcEligibleOtHours(otSession);
        int minimumOtHours = officePolicyService.otMinHours(otSession.getOffice());
        if (eligibleOtHours < minimumOtHours) {
            throw new BadRequestException("Eligible OT hours are below office minimum OT hours");
        }

        String reportNoteFinal = normalizeReportNoteOrThrow(request.getReportNote());

        OtReport report = new OtReport();
        report.setId(UUID.randomUUID());
        report.setAttendanceLog(attendanceLog);
        report.setOtRequest(approvedRequest);
        report.setOtSession(otSession);
        report.setReportedOtHours(eligibleOtHours);
        report.setReportNote(reportNoteFinal);
        report.setStatus(otReportWorkflowService.initialStatus(actor.getRole()));

        String uploadedPublicId = null;
        try {
            uploadedPublicId = applyEvidenceOrNull(report, evidenceFile);
            otReportRepository.save(report);
        } catch (RuntimeException ex) {
            cleanupEvidenceSilently(uploadedPublicId);
            throw ex;
        }
        return otReportMapper.toResponse(report);
    }

    private String applyEvidenceOrNull(OtReport report, MultipartFile evidenceFile) {
        if (evidenceFile == null || evidenceFile.isEmpty()) {
            return null;
        }
        if (cloudinary == null) {
            throw new BadRequestException("Evidence upload is not available right now");
        }
        if (evidenceFile.getSize() > MAX_EVIDENCE_SIZE_BYTES) {
            throw new BadRequestException("Evidence file must be <= 8MB");
        }

        String mimeType = evidenceFile.getContentType();
        if (mimeType == null || !ALLOWED_EVIDENCE_MIME_TYPES.contains(mimeType.toLowerCase())) {
            throw new BadRequestException("Unsupported evidence file type");
        }

        String safeFileName = normalizeEvidenceFileName(evidenceFile.getOriginalFilename());
        String publicId = "ot_reports/" + report.getId() + "/evidence";
        try {
            Map<?, ?> uploadResult = cloudinary.uploader().upload(
                    evidenceFile.getBytes(),
                    ObjectUtils.asMap(
                            "public_id", publicId,
                            "resource_type", "auto",
                            "overwrite", true,
                            "invalidate", true
                    )
            );
            Object secureUrl = uploadResult.get("secure_url");
            if (secureUrl == null) {
                throw new BadRequestException("Evidence upload failed: missing file URL");
            }

            report.setEvidenceFileName(safeFileName);
            report.setEvidenceFileUrl(secureUrl.toString());
            report.setEvidenceFilePublicId(publicId);
            report.setEvidenceFileMimeType(mimeType);
            report.setEvidenceFileSizeBytes(evidenceFile.getSize());
            return publicId;
        } catch (BadRequestException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BadRequestException("Evidence upload failed. Please try again.");
        }
    }

    private String normalizeEvidenceFileName(String originalFilename) {
        String fallback = "evidence-file";
        if (originalFilename == null || originalFilename.isBlank()) {
            return fallback;
        }
        String normalized = originalFilename.trim();
        if (normalized.length() <= 255) {
            return normalized;
        }
        return normalized.substring(0, 255);
    }

    private void cleanupEvidenceSilently(String publicId) {
        if (publicId == null || cloudinary == null) {
            return;
        }
        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "raw", "invalidate", true));
            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("resource_type", "image", "invalidate", true));
        } catch (Exception ex) {
            log.warn("Failed to rollback uploaded OT evidence {}", publicId, ex);
        }
    }

    private String normalizeReportNoteOrThrow(String reportNote) {
        String normalized = reportNote != null ? reportNote.trim() : "";
        if (normalized.isBlank()) {
            throw new BadRequestException("Report note (evidence) is required");
        }
        if (normalized.length() < REPORT_NOTE_MIN_LENGTH) {
            throw new BadRequestException("Report note must be at least " + REPORT_NOTE_MIN_LENGTH + " characters");
        }
        if (normalized.length() > REPORT_NOTE_MAX_LENGTH) {
            throw new BadRequestException("Report note exceeds maximum length of " + REPORT_NOTE_MAX_LENGTH + " characters");
        }
        return normalized;
    }

    private int calcEligibleOtHours(OtSession otSession) {
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
        OtReport current = otReportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("OT report not found"));
//        assertExpectedVersion(request.getExpectedVersion(), report.getVersion(), "OT report");
        if (request.getExpectedVersion() == null) {
            throw new BadRequestException("Expected version is required");
        }

        User manager = accessScopeService.currentUserOrThrow();
        boolean approved = Boolean.TRUE.equals(request.getApproved());

        boolean isSelfReport = current.getAttendanceLog() != null
                && current.getAttendanceLog().getUser() != null
                && current.getAttendanceLog().getUser().getId() != null
                && current.getAttendanceLog().getUser().getId().equals(manager.getId());

        if (isSelfReport && !accessScopeService.isOfficeManager(manager)) {
            throw new ForbiddenException("You cannot approve your own OT report");
        }

        if (accessScopeService.isDepartmentManager(manager)) {
            UUID departmentId = current.getAttendanceLog() != null
                    && current.getAttendanceLog().getUser() != null
                    && current.getAttendanceLog().getUser().getDepartment() != null
                    ? current.getAttendanceLog().getUser().getDepartment().getId()
                    : null;
            accessScopeService.assertCanManageDepartment(departmentId);
        } else if (accessScopeService.isOfficeManager(manager)) {
            UUID officeId = current.getAttendanceLog() != null
                    && current.getAttendanceLog().getOffice() != null
                    ? current.getAttendanceLog().getOffice().getId()
                    : null;
            accessScopeService.assertCanManageOffice(officeId);
        } else {
            throw new ForbiddenException("You do not have permission to decide OT reports");
        }

        OtReport report = new OtReport();
        report.setId(current.getId());
        report.setVersion(request.getExpectedVersion());
        report.setAttendanceLog(current.getAttendanceLog());
        report.setOtRequest(current.getOtRequest());
        report.setOtSession(current.getOtSession());
        report.setReportedOtHours(current.getReportedOtHours());
        report.setReportNote(current.getReportNote());
        report.setEvidenceFileName(current.getEvidenceFileName());
        report.setEvidenceFileUrl(current.getEvidenceFileUrl());
        report.setEvidenceFilePublicId(current.getEvidenceFilePublicId());
        report.setEvidenceFileMimeType(current.getEvidenceFileMimeType());
        report.setEvidenceFileSizeBytes(current.getEvidenceFileSizeBytes());
        report.setStatus(otReportWorkflowService.nextStatus(manager.getRole(), current.getStatus(), approved));
        report.setApprovedBy(manager);
        report.setApprovedAt(LocalDateTime.now());
        report.setDecisionNote(request.getDecisionNote());
        OtReport merged = mergeAndFlush(report);
        return otReportMapper.toResponse(merged);
    }

//    private void assertExpectedVersion(Long expectedVersion, Long currentVersion, String resourceName) {
//        if (expectedVersion == null) {
//            throw new BadRequestException("Expected version is required");
//        }
//        if (!Objects.equals(expectedVersion, currentVersion)) {
//            throw new ConflictException(resourceName + " was modified by another user. Please refresh and retry.");
//        }
//    }

    private OtReport mergeAndFlush(OtReport report) {
        if (entityManager != null) {
            OtReport merged = entityManager.merge(report);
            entityManager.flush();
            return merged;
        }
        return otReportRepository.saveAndFlush(report);
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
                    .map(otReportMapper::toResponse)
                    .toList();
        }

        if (accessScopeService.isOfficeManager(currentApprover)) {
            return otReportRepository
                    .findAllByAttendanceLog_User_Office_IdOrderByAttendanceLog_LogDateDesc(
                            currentApprover.getOffice().getId()
                    )
                    .stream()
                    .map(otReportMapper::toResponse)
                    .toList();
        }

        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OtReportResponse> listPendingForScope() {
        User currentApprover = accessScopeService.currentUserOrThrow();

        if (accessScopeService.isDepartmentManager(currentApprover)) {
            List<OtReport> pendingReports = new ArrayList<>();
            for (OtReportStatus status : otReportWorkflowService.pendingForDeptMgr()) {
                pendingReports.addAll(
                        otReportRepository.findByAttendanceLog_User_Department_IdAndStatusOrderByAttendanceLog_LogDateDesc(
                                currentApprover.getDepartment().getId(),
                                status
                        )
                );
            }
            pendingReports.sort(comparing(r -> r.getAttendanceLog().getLogDate(), java.util.Comparator.reverseOrder()));
            return pendingReports.stream().map(otReportMapper::toResponse).toList();
        }

        if (accessScopeService.isOfficeManager(currentApprover)) {
            List<OtReport> pendingReports = new ArrayList<>();
            for (OtReportStatus status : otReportWorkflowService.pendingForOfficeMgr()) {
                pendingReports.addAll(
                        otReportRepository.findByAttendanceLog_Office_IdAndStatusOrderByAttendanceLog_LogDateDesc(
                                currentApprover.getOffice().getId(),
                                status
                        )
                );
            }
            pendingReports.sort(comparing(r -> r.getAttendanceLog().getLogDate(), java.util.Comparator.reverseOrder()));
            return pendingReports.stream().map(otReportMapper::toResponse).toList();
        }

        return List.of();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OtReportResponse> listMy() {
        User actor = accessScopeService.currentUserOrThrow();
        return otReportRepository.findByAttendanceLog_User_IdOrderByAttendanceLog_LogDateDesc(actor.getId())
                .stream()
                .map(otReportMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<OtReportResponse> listBySession(UUID otSessionId) {
        return otReportRepository.findByOtSession_IdOrderByAttendanceLog_LogDateDesc(otSessionId)
                .stream()
                .map(otReportMapper::toResponse)
                .toList();
    }
}

