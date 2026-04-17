package com.deha.HumanResourceManagement.entity;

import com.deha.HumanResourceManagement.entity.enums.OtRequestStatus;
import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "ot_requests",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "log_date"})
)
@Getter
@Setter
public class OtRequest extends AuditableByUser {

    @Id
    @UuidGenerator
    @EqualsAndHashCode.Include
    private UUID id;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "office_id", nullable = false)
    private Office office;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @Column(name = "reason", nullable = false, length = 500)
    private String reason;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private OtRequestStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "decision_note", length = 500)
    private String decisionNote;
}