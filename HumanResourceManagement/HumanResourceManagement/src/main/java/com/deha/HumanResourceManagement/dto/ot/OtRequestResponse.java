package com.deha.HumanResourceManagement.dto.ot;

import com.deha.HumanResourceManagement.entity.enums.OtRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtRequestResponse {
    private UUID id;
    private Long version;
    private UUID userId;
    private String userName;
    private UUID officeId;
    private LocalDate logDate;
    private String reason;
    private OtRequestStatus status;
    private UUID approvedById;
    private String approvedByName;
    private LocalDateTime approvedAt;
    private String decisionNote;
}
