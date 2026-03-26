package com.deha.HumanResourceManagement.dto.ot;

import com.deha.HumanResourceManagement.entity.OtRequest;
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

    public static OtRequestResponse fromEntity(OtRequest request) {
        if (request == null) return null;
        return new OtRequestResponse(
                request.getId(),
                request.getUser() != null ? request.getUser().getId() : null,
                request.getUser() != null ? (request.getUser().getFirstName() + " " + request.getUser().getLastName()).trim() : null,
                request.getOffice() != null ? request.getOffice().getId() : null,
                request.getLogDate(),
                request.getReason(),
                request.getStatus(),
                request.getApprovedBy() != null ? request.getApprovedBy().getId() : null,
                request.getApprovedBy() != null ? (request.getApprovedBy().getFirstName() + " " + request.getApprovedBy().getLastName()).trim() : null,
                request.getApprovedAt(),
                request.getDecisionNote()
        );
    }
}

