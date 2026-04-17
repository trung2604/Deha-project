package com.deha.HumanResourceManagement.entity;

import com.deha.HumanResourceManagement.entity.enums.OtSessionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "ot_sessions",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "log_date"})
)
@Getter
@Setter
public class OtSession extends AuditableByUser {
    @Id
    @UuidGenerator
    private UUID id;

    @Version
    private Long version;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "office_id", nullable = false)
    private Office office;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ot_request_id", nullable = false)
    private OtRequest otRequest;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @Column(name = "check_in_time", nullable = false)
    private LocalDateTime checkInTime;

    @Column(name = "check_out_time")
    private LocalDateTime checkOutTime;

    @Column(name = "source", nullable = false, length = 16)
    private String source;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private OtSessionStatus status;
}
