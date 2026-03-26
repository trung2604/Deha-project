package com.deha.HumanResourceManagement.dto.ot;

import com.deha.HumanResourceManagement.entity.OtReport;
import com.deha.HumanResourceManagement.entity.enums.OtReportStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtReportResponse {
    private UUID id;
    private UUID attendanceLogId;
    private UUID otRequestId;
    private Integer reportedOtHours;
    private String reportNote;
    private String evidenceFileName;
    private OtReportStatus status;
    private UUID approvedById;
    private String approvedByName;
    private LocalDateTime approvedAt;
    private String decisionNote;

    public static OtReportResponse fromEntity(OtReport report) {
        if (report == null) return null;
        return new OtReportResponse(
                report.getId(),
                report.getAttendanceLog() != null ? report.getAttendanceLog().getId() : null,
                report.getOtRequest() != null ? report.getOtRequest().getId() : null,
                report.getReportedOtHours(),
                report.getReportNote(),
                report.getEvidenceFileName(),
                report.getStatus(),
                report.getApprovedBy() != null ? report.getApprovedBy().getId() : null,
                report.getApprovedBy() != null ? (report.getApprovedBy().getFirstName() + " " + report.getApprovedBy().getLastName()).trim() : null,
                report.getApprovedAt(),
                report.getDecisionNote()
        );
    }
}

