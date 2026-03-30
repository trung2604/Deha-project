package com.deha.HumanResourceManagement.entity;

import com.deha.HumanResourceManagement.entity.enums.OtType;
import com.deha.HumanResourceManagement.entity.enums.CheckoutSource;
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
        name = "attendance_logs",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "log_date"})
)
@Getter
@Setter
public class AttendanceLog {

    @Id
    @UuidGenerator
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "office_id", nullable = false)
    private Office office;

    @Column(name = "check_in_time", nullable = false)
    private LocalDateTime checkInTime;

    @Column(name = "check_out_time")
    private LocalDateTime checkOutTime;

    @Column(name = "worked_hours")
    private Integer workedHours;

    @Column(name = "ot_hours")
    private Integer otHours;

    @Enumerated(EnumType.STRING)
    @Column(name = "ot_type")
    private OtType otType;

    @Enumerated(EnumType.STRING)
    @Column(name = "checkout_source", nullable = false, length = 16)
    private CheckoutSource checkoutSource = CheckoutSource.MANUAL;

    @Column(name = "auto_checked_out", nullable = false)
    private Boolean autoCheckedOut = false;

    @Column(name = "client_ip", length = 45)
    private String clientIp;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    public void checkIn(User user, Office office, String clientIp) {
        this.user            = user;
        this.office          = office;
        this.clientIp        = clientIp;
        this.checkInTime     = LocalDateTime.now();
        this.logDate         = LocalDate.now();
        this.checkoutSource  = CheckoutSource.MANUAL;
        this.autoCheckedOut  = false;
    }
}