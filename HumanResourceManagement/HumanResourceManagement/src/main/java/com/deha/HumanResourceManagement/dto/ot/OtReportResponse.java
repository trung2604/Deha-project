package com.deha.HumanResourceManagement.dto.ot;

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
}
