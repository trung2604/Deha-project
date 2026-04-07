package com.deha.HumanResourceManagement.dto.ot;

import com.deha.HumanResourceManagement.entity.OtReport;
import com.deha.HumanResourceManagement.entity.enums.OtReportStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtReportResponse {
    private UUID id;
    private Long version;
    private UUID userId;
    private String userName;
    private LocalDate logDate;
    private UUID attendanceLogId;
    private UUID otRequestId;
    private UUID otSessionId;
    private Integer reportedOtHours;
    private String reportNote;
    private String evidenceFileName;
    private String evidenceFileUrl;
    private String evidenceFileMimeType;
    private Long evidenceFileSizeBytes;
    private OtReportStatus status;
    private UUID approvedById;
    private String approvedByName;
    private LocalDateTime approvedAt;
    private String decisionNote;

    public static OtReportResponse fromEntity(OtReport report) {
        if (report == null) return null;
        return new OtReportResponse(
                report.getId(),
                report.getVersion(),
                report.getAttendanceLog() != null && report.getAttendanceLog().getUser() != null
                        ? report.getAttendanceLog().getUser().getId()
                        : null,
                report.getAttendanceLog() != null && report.getAttendanceLog().getUser() != null
                        ? (report.getAttendanceLog().getUser().getFirstName() + " " + report.getAttendanceLog().getUser().getLastName()).trim()
                        : null,
                report.getAttendanceLog() != null ? report.getAttendanceLog().getLogDate() : null,
                report.getAttendanceLog() != null ? report.getAttendanceLog().getId() : null,
                report.getOtRequest() != null ? report.getOtRequest().getId() : null,
                report.getOtSession() != null ? report.getOtSession().getId() : null,
                report.getReportedOtHours(),
                report.getReportNote(),
                report.getEvidenceFileName(),
                report.getEvidenceFileUrl(),
                report.getEvidenceFileMimeType(),
                report.getEvidenceFileSizeBytes(),
                report.getStatus(),
                report.getApprovedBy() != null ? report.getApprovedBy().getId() : null,
                report.getApprovedBy() != null ? (report.getApprovedBy().getFirstName() + " " + report.getApprovedBy().getLastName()).trim() : null,
                report.getApprovedAt(),
                report.getDecisionNote()
        );
    }
}

