package com.deha.HumanResourceManagement.dto.notification;

import com.deha.HumanResourceManagement.entity.enums.NotificationType;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class NotificationResponse {

    private UUID id;
    private NotificationType type;
    private String title;
    private String body;
    private String referenceId;
    private boolean isRead;
    private Instant createdAt;
}
