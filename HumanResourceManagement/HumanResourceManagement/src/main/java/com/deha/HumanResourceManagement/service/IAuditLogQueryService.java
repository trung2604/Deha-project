package com.deha.HumanResourceManagement.service;

import com.deha.HumanResourceManagement.dto.audit.AuditLogResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.UUID;

public interface IAuditLogQueryService {
    Page<AuditLogResponse> getAuditLogs(
            UUID actorUserId,
            String httpMethod,
            String endpointPattern,
            Integer statusCode,
            Boolean success,
            UUID targetId,
            Instant from,
            Instant to,
            Pageable pageable
    );

    AuditLogResponse getAuditLogById(UUID id);

    String exportAuditLogsCsv(
            UUID actorUserId,
            String httpMethod,
            String endpointPattern,
            Integer statusCode,
            Boolean success,
            UUID targetId,
            Instant from,
            Instant to,
            Integer limit
    );
}

