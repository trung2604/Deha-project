package com.deha.HumanResourceManagement.service.support;

import com.deha.HumanResourceManagement.entity.AuditLog;
import com.deha.HumanResourceManagement.repository.AuditLogRepository;
import com.deha.HumanResourceManagement.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.HandlerMapping;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuditLogService {
    private static final Logger log = LoggerFactory.getLogger(AuditLogService.class);

    public static final String ATTR_AUDIT_START_NANOS = "audit.startNanos";

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public AuditLogService(AuditLogRepository auditLogRepository, UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void logHttpWrite(HttpServletRequest request, int statusCode, long durationMs) {
        try {
            AuditLog entry = new AuditLog();
            entry.setHttpMethod(safe(request.getMethod()));
            entry.setEndpointPattern(resolveEndpointPattern(request));
            entry.setRequestUri(safe(request.getRequestURI()));
            entry.setTargetId(resolveTargetId(request));
            entry.setStatusCode(statusCode);
            entry.setSuccess(statusCode >= 200 && statusCode < 400);
            entry.setClientIp(extractClientIp(request));
            entry.setUserAgent(trimToLength(request.getHeader("User-Agent"), 500));
            entry.setDurationMs(durationMs < 0 ? null : durationMs);

            resolveActor().ifPresent(actor -> {
                entry.setActorEmail(actor.email());
                entry.setActorUserId(actor.userId());
                entry.setActorOfficeId(actor.officeId());
            });

            auditLogRepository.save(entry);
        } catch (Exception ex) {
            log.warn("Failed to persist audit log", ex);
        }
    }

    private Optional<ActorContext> resolveActor() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null || auth.getName().isBlank()) {
            return Optional.empty();
        }

        String email = auth.getName().trim().toLowerCase(Locale.ROOT);
        return Optional.of(
                userRepository.findByEmail(email)
                        .map(user -> {
                            UUID officeId = user.getOffice() != null ? user.getOffice().getId() : null;
                            return new ActorContext(email, user.getId(), officeId);
                        })
                        .orElse(new ActorContext(email, null, null))
        );
    }

    @SuppressWarnings("unchecked")
    private UUID resolveTargetId(HttpServletRequest request) {
        Object attr = request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        if (!(attr instanceof Map<?, ?> vars)) {
            return null;
        }
        Object idValue = vars.get("id");
        if (idValue == null) {
            return null;
        }
        try {
            return UUID.fromString(idValue.toString());
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private String resolveEndpointPattern(HttpServletRequest request) {
        Object pattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
        if (pattern == null) {
            return safe(request.getRequestURI());
        }
        return trimToLength(pattern.toString(), 255);
    }

    private String extractClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            String first = forwardedFor.split(",")[0].trim();
            return trimToLength(first, 45);
        }
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return trimToLength(realIp.trim(), 45);
        }
        return trimToLength(request.getRemoteAddr(), 45);
    }

    private String safe(String value) {
        return trimToLength(value == null ? "" : value, 500);
    }

    private String trimToLength(String value, int maxLength) {
        if (value == null) {
            return null;
        }
        if (value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength);
    }

    private record ActorContext(String email, UUID userId, UUID officeId) {
    }
}

