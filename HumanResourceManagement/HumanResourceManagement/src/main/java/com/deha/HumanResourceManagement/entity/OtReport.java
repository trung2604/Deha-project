package com.deha.HumanResourceManagement.entity;

import com.deha.HumanResourceManagement.entity.enums.OtReportStatus;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "ot_reports",
        uniqueConstraints = @UniqueConstraint(columnNames = {"attendance_log_id"})
)
@Getter
@Setter
public class OtReport {

    @Id
    @UuidGenerator
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attendance_log_id", nullable = false)
    private AttendanceLog attendanceLog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ot_request_id", nullable = false)
    private OtRequest otRequest;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ot_session_id")
    private OtSession otSession;

    @Column(name = "reported_ot_hours", nullable = false)
    private Integer reportedOtHours;

    @Column(name = "report_note", nullable = false, length = 500)
    private String reportNote;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private OtReportStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "decision_note", length = 500)
    private String decisionNote;

    @Column(name = "evidence_file_name", length = 255)
    private String evidenceFileName;
}