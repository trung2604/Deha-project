package com.deha.HumanResourceManagement.dto.audit;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class AuditLogResponse {
    private UUID id;
    private UUID actorUserId;
    private UUID actorOfficeId;
    private String actorEmail;
    private String httpMethod;
    private String endpointPattern;
    private String requestUri;
    private UUID targetId;
    private Integer statusCode;
    private Boolean success;
    private String clientIp;
    private String userAgent;
    private Long durationMs;
    private Instant occurredAt;
}

