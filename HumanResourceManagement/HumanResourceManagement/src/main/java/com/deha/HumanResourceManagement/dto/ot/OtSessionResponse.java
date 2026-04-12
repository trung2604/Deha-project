package com.deha.HumanResourceManagement.dto.ot;

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
    private Long version;
    private UUID otRequestId;
    private LocalDate logDate;
    private LocalDateTime checkInTime;
    private LocalDateTime checkOutTime;
    private Integer minimumOtHours;
    private String source;
    private OtSessionStatus status;
}
