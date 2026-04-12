package com.deha.HumanResourceManagement.repository.specification;

import com.deha.HumanResourceManagement.entity.AuditLog;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collection;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;

public class AuditLogSpecification {

    public static Specification<AuditLog> hasActorUserId(UUID actorUserId) {
        return (root, query, cb) ->
                actorUserId == null ? null : cb.equal(root.get("actorUserId"), actorUserId);
    }

    public static Specification<AuditLog> actorUserIdIn(Collection<UUID> actorUserIds) {
        return (root, query, cb) -> {
            if (actorUserIds == null || actorUserIds.isEmpty()) {
                return cb.disjunction();
            }
            return root.get("actorUserId").in(actorUserIds);
        };
    }

    public static Specification<AuditLog> hasActorOfficeId(UUID actorOfficeId) {
        return (root, query, cb) ->
                actorOfficeId == null ? cb.disjunction() : cb.equal(root.get("actorOfficeId"), actorOfficeId);
    }

    public static Specification<AuditLog> hasHttpMethod(String httpMethod) {
        return (root, query, cb) -> {
            if (httpMethod == null || httpMethod.isBlank()) {
                return null;
            }
            return cb.equal(root.get("httpMethod"), httpMethod.trim().toUpperCase(Locale.ROOT));
        };
    }

    public static Specification<AuditLog> endpointContains(String endpointPattern) {
        return (root, query, cb) -> {
            if (endpointPattern == null || endpointPattern.isBlank()) {
                return null;
            }
            String pattern = "%" + endpointPattern.trim().toLowerCase(Locale.ROOT) + "%";
            return cb.like(cb.lower(root.get("endpointPattern")), pattern);
        };
    }

    public static Specification<AuditLog> hasSuccess(Boolean success) {
        return (root, query, cb) ->
                success == null ? null : cb.equal(root.get("success"), success);
    }

    public static Specification<AuditLog> hasStatusCode(Integer statusCode) {
        return (root, query, cb) ->
                statusCode == null ? null : cb.equal(root.get("statusCode"), statusCode);
    }

    public static Specification<AuditLog> hasTargetId(UUID targetId) {
        return (root, query, cb) ->
                targetId == null ? null : cb.equal(root.get("targetId"), targetId);
    }

    public static Specification<AuditLog> occurredFrom(Instant from) {
        return (root, query, cb) ->
                from == null ? null : cb.greaterThanOrEqualTo(root.get("occurredAt"), from);
    }

    public static Specification<AuditLog> occurredTo(Instant to) {
        return (root, query, cb) ->
                to == null ? null : cb.lessThanOrEqualTo(root.get("occurredAt"), to);
    }
}

