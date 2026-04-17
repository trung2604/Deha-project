package com.deha.HumanResourceManagement.config;

import com.deha.HumanResourceManagement.service.support.AuditLogService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

@Component
public class AuditLoggingInterceptor implements HandlerInterceptor {
    private static final Set<String> WRITE_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");
    private static final String ATTR_AUDIT_ENABLED = "audit.enabled";

    private static final Set<String> AUDITED_SYSTEM_WRITE_PATTERNS = Set.of(
            "/api/users/**",
            "/api/departments/**",
            "/api/positions/**",
            "/api/offices/**",
            "/api/salary-contracts/**",
            "/api/payrolls/**",
            "/api/ot-requests/**",
            "/api/ot-reports/**",
            "/api/ot-sessions/**"
    );

    private static final Set<String> AUDITED_AUTH_SECURITY_PATHS = Set.of(
            "/api/auth/login",
            "/api/auth/refresh",
            "/api/auth/oauth2/exchange",
            "/api/auth/logout",
            "/api/auth/forgot-password",
            "/api/auth/verify-otp",
            "/api/auth/reset-password",
            "/api/auth/change-password",
            "/api/auth/me"
    );

    private final AuditLogService auditLogService;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    public AuditLoggingInterceptor(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        boolean shouldAudit = shouldAuditRequest(request);
        request.setAttribute(ATTR_AUDIT_ENABLED, shouldAudit);
        if (shouldAudit) {
            request.setAttribute(AuditLogService.ATTR_AUDIT_START_NANOS, System.nanoTime());
        }
        return true;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler,
            Exception ex
    ) {
        if (!Boolean.TRUE.equals(request.getAttribute(ATTR_AUDIT_ENABLED))) {
            return;
        }
        Object startObj = request.getAttribute(AuditLogService.ATTR_AUDIT_START_NANOS);
        long durationMs = -1L;
        if (startObj instanceof Long startNanos) {
            durationMs = (System.nanoTime() - startNanos) / 1_000_000L;
        }
        auditLogService.logHttpWrite(request, response.getStatus(), durationMs);
    }

    private boolean isWriteMethod(String method) {
        return method != null && WRITE_METHODS.contains(method);
    }

    private boolean shouldAuditRequest(HttpServletRequest request) {
        String method = request.getMethod();
        if (!isWriteMethod(method)) {
            return false;
        }

        String path = request.getRequestURI();
        if (path == null || path.isBlank()) {
            return false;
        }

        if ("DELETE".equals(method)) {
            return pathMatcher.match("/api/**", path);
        }

        if (AUDITED_AUTH_SECURITY_PATHS.contains(path)) {
            return true;
        }

        return AUDITED_SYSTEM_WRITE_PATTERNS.stream().anyMatch(pattern -> pathMatcher.match(pattern, path));
    }
}

