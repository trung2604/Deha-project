package com.deha.HumanResourceManagement.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "audit_logs",
        indexes = {
                @Index(name = "idx_audit_logs_occurred_at", columnList = "occurred_at DESC"),
                @Index(name = "idx_audit_logs_actor_user", columnList = "actor_user_id, occurred_at DESC"),
                @Index(name = "idx_audit_logs_actor_office", columnList = "actor_office_id, occurred_at DESC"),
                @Index(name = "idx_audit_logs_method_path", columnList = "http_method, endpoint_pattern")
        })
@Getter
@Setter
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "actor_user_id")
    private UUID actorUserId;

    @Column(name = "actor_email", length = 100)
    private String actorEmail;

    @Column(name = "actor_office_id")
    private UUID actorOfficeId;

    @Column(name = "http_method", nullable = false, length = 10)
    private String httpMethod;

    @Column(name = "endpoint_pattern", nullable = false, length = 255)
    private String endpointPattern;

    @Column(name = "request_uri", nullable = false, length = 500)
    private String requestUri;

    @Column(name = "target_id")
    private UUID targetId;

    @Column(name = "status_code", nullable = false)
    private Integer statusCode;

    @Column(name = "success", nullable = false)
    private Boolean success;

    @Column(name = "client_ip", length = 45)
    private String clientIp;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "duration_ms")
    private Long durationMs;

    @CreationTimestamp
    @Column(name = "occurred_at", nullable = false, updatable = false)
    private Instant occurredAt;
}

