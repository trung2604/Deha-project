package com.deha.HumanResourceManagement.dto.notification;

import com.deha.HumanResourceManagement.entity.Notification;
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

    public static NotificationResponse fromEntity(Notification n) {
        NotificationResponse r = new NotificationResponse();
        r.id = n.getId();
        r.type = n.getType();
        r.title = n.getTitle();
        r.body = n.getBody();
        r.referenceId = n.getReferenceId();
        r.isRead = n.isRead();
        r.createdAt = n.getCreatedAt();
        return r;
    }
}
