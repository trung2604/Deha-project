package com.deha.HumanResourceManagement.dto.attendance;

import com.deha.HumanResourceManagement.entity.AttendanceLog;
import com.deha.HumanResourceManagement.entity.enums.CheckoutSource;
import com.deha.HumanResourceManagement.entity.enums.OtType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceLogResponse {
    private UUID id;
    private UUID userId;
    private String userName;
    private UUID officeId;
    private LocalDate logDate;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private Integer workedHours;
    private Integer otHours;
    private OtType otType;
    private CheckoutSource checkoutSource;
    private Boolean autoCheckedOut;
    private String clientIp;

    public static AttendanceLogResponse fromEntity(AttendanceLog log) {
        if (log == null) return null;
        return new AttendanceLogResponse(
                log.getId(),
                log.getUser() != null ? log.getUser().getId() : null,
                log.getUser() != null ? (log.getUser().getFirstName() + " " + log.getUser().getLastName()).trim() : null,
                log.getOffice() != null ? log.getOffice().getId() : null,
                log.getLogDate(),
                log.getCheckInTime(),
                log.getCheckOutTime(),
                log.getWorkedHours(),
                log.getOtHours(),
                log.getOtType(),
                log.getCheckoutSource(),
                log.getAutoCheckedOut(),
                log.getClientIp()
        );
    }
}

