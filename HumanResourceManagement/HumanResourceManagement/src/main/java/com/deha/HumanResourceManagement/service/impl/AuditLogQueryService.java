package com.deha.HumanResourceManagement.service.impl;

import com.deha.HumanResourceManagement.config.security.AccessScopeService;
import com.deha.HumanResourceManagement.dto.audit.AuditLogResponse;
import com.deha.HumanResourceManagement.entity.AuditLog;
import com.deha.HumanResourceManagement.entity.User;
import com.deha.HumanResourceManagement.exception.ForbiddenException;
import com.deha.HumanResourceManagement.exception.ResourceNotFoundException;
import com.deha.HumanResourceManagement.repository.AuditLogRepository;
import com.deha.HumanResourceManagement.repository.specification.AuditLogSpecification;
import com.deha.HumanResourceManagement.service.IAuditLogQueryService;
import com.deha.HumanResourceManagement.service.support.AuditLogCsvExporter;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class AuditLogQueryService implements IAuditLogQueryService {
    private static final int DEFAULT_EXPORT_LIMIT = 5000;
    private static final int MAX_EXPORT_LIMIT = 10000;

    private final AuditLogRepository auditLogRepository;
    private final AccessScopeService accessScopeService;
    private final AuditLogCsvExporter auditLogCsvExporter;

    public AuditLogQueryService(
            AuditLogRepository auditLogRepository,
            AccessScopeService accessScopeService,
            AuditLogCsvExporter auditLogCsvExporter
    ) {
        this.auditLogRepository = auditLogRepository;
        this.accessScopeService = accessScopeService;
        this.auditLogCsvExporter = auditLogCsvExporter;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditLogResponse> getAuditLogs(
            UUID actorUserId,
            String httpMethod,
            String endpointPattern,
            Integer statusCode,
            Boolean success,
            UUID targetId,
            Instant from,
            Instant to,
            Pageable pageable
    ) {
        User actor = accessScopeService.currentUserOrThrow();

        Specification<AuditLog> spec = buildScopedSpecification(
                actor,
                actorUserId,
                httpMethod,
                endpointPattern,
                statusCode,
                success,
                targetId,
                from,
                to
        );

        return auditLogRepository.findAll(spec, pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public String exportAuditLogsCsv(
            UUID actorUserId,
            String httpMethod,
            String endpointPattern,
            Integer statusCode,
            Boolean success,
            UUID targetId,
            Instant from,
            Instant to,
            Integer limit
    ) {
        User actor = accessScopeService.currentUserOrThrow();
        Specification<AuditLog> spec = buildScopedSpecification(
                actor,
                actorUserId,
                httpMethod,
                endpointPattern,
                statusCode,
                success,
                targetId,
                from,
                to
        );

        int safeLimit = resolveExportLimit(limit);
        PageRequest pageRequest = PageRequest.of(0, safeLimit, Sort.by(Sort.Direction.DESC, "occurredAt"));
        List<AuditLogResponse> rows = auditLogRepository.findAll(spec, pageRequest)
                .map(this::toResponse)
                .getContent();
        return auditLogCsvExporter.toCsv(rows);
    }

    @Override
    @Transactional(readOnly = true)
    public AuditLogResponse getAuditLogById(UUID id) {
        User actor = accessScopeService.currentUserOrThrow();
        AuditLog log = auditLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Audit log not found"));

        if (accessScopeService.isOfficeManager(actor)) {
            UUID actorOfficeId = actor.getOffice() != null ? actor.getOffice().getId() : null;
            if (actorOfficeId == null) {
                throw new ForbiddenException("Office manager must be assigned to an office");
            }
            if (!actorOfficeId.equals(log.getActorOfficeId())) {
                throw new ForbiddenException("Office manager can only access audit logs in their own office");
            }
        } else if (!accessScopeService.isAdmin(actor)) {
            throw new ForbiddenException("You do not have permission to view audit logs");
        }

        return toResponse(log);
    }

    private Specification<AuditLog> buildScopedSpecification(
            User actor,
            UUID actorUserId,
            String httpMethod,
            String endpointPattern,
            Integer statusCode,
            Boolean success,
            UUID targetId,
            Instant from,
            Instant to
    ) {
        Specification<AuditLog> spec = Specification
                .where(AuditLogSpecification.hasActorUserId(actorUserId))
                .and(AuditLogSpecification.hasHttpMethod(httpMethod))
                .and(AuditLogSpecification.endpointContains(endpointPattern))
                .and(AuditLogSpecification.hasStatusCode(statusCode))
                .and(AuditLogSpecification.hasSuccess(success))
                .and(AuditLogSpecification.hasTargetId(targetId))
                .and(AuditLogSpecification.occurredFrom(from))
                .and(AuditLogSpecification.occurredTo(to));

        if (accessScopeService.isOfficeManager(actor)) {
            UUID actorOfficeId = actor.getOffice() != null ? actor.getOffice().getId() : null;
            if (actorOfficeId == null) {
                throw new ForbiddenException("Office manager must be assigned to an office");
            }
            return spec.and(AuditLogSpecification.hasActorOfficeId(actorOfficeId));
        }
        if (accessScopeService.isAdmin(actor)) {
            return spec;
        }
        throw new ForbiddenException("You do not have permission to view audit logs");
    }

    private int resolveExportLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_EXPORT_LIMIT;
        }
        return Math.min(limit, MAX_EXPORT_LIMIT);
    }

    private AuditLogResponse toResponse(AuditLog entity) {
        AuditLogResponse response = new AuditLogResponse();
        response.setId(entity.getId());
        response.setActorUserId(entity.getActorUserId());
        response.setActorOfficeId(entity.getActorOfficeId());
        response.setActorEmail(entity.getActorEmail());
        response.setHttpMethod(entity.getHttpMethod());
        response.setEndpointPattern(entity.getEndpointPattern());
        response.setRequestUri(entity.getRequestUri());
        response.setTargetId(entity.getTargetId());
        response.setStatusCode(entity.getStatusCode());
        response.setSuccess(entity.getSuccess());
        response.setClientIp(entity.getClientIp());
        response.setUserAgent(entity.getUserAgent());
        response.setDurationMs(entity.getDurationMs());
        response.setOccurredAt(entity.getOccurredAt());
        return response;
    }
}

