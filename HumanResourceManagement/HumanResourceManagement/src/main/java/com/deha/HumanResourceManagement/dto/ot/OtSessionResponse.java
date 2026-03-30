package com.deha.HumanResourceManagement.dto.ot;

import com.deha.HumanResourceManagement.entity.OtSession;
import com.deha.HumanResourceManagement.entity.enums.OtSessionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtSessionResponse {
    private UUID id;
    private UUID otRequestId;
    private LocalDate logDate;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private String source;
    private OtSessionStatus status;

    public static OtSessionResponse fromEntity(OtSession session) {
        if (session == null) return null;
        return new OtSessionResponse(
                session.getId(),
                session.getOtRequest() != null ? session.getOtRequest().getId() : null,
                session.getLogDate(),
                session.getCheckInTime(),
                session.getCheckOutTime(),
                session.getSource(),
                session.getStatus()
        );
    }
}
