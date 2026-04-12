package com.deha.HumanResourceManagement.controller;

import com.deha.HumanResourceManagement.dto.ApiResponse;
import com.deha.HumanResourceManagement.dto.PageResponse;
import com.deha.HumanResourceManagement.dto.audit.AuditLogResponse;
import com.deha.HumanResourceManagement.service.IAuditLogQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Tag(name = "Audit Logs", description = "Audit log query APIs for system write actions")
@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController extends ApiControllerSupport {

    private final IAuditLogQueryService auditLogQueryService;

    public AuditLogController(IAuditLogQueryService auditLogQueryService) {
        this.auditLogQueryService = auditLogQueryService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER_OFFICE')")
    @Operation(summary = "List audit logs", description = "Get paged audit logs with optional filters")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Audit logs retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ApiResponse listAuditLogs(
            @RequestParam(required = false) UUID actorUserId,
            @RequestParam(required = false) String httpMethod,
            @RequestParam(required = false) String endpointPattern,
            @RequestParam(required = false) Integer statusCode,
            @RequestParam(required = false) Boolean success,
            @RequestParam(required = false) UUID targetId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @PageableDefault(size = 20, sort = "occurredAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<AuditLogResponse> results = auditLogQueryService.getAuditLogs(
                actorUserId,
                httpMethod,
                endpointPattern,
                statusCode,
                success,
                targetId,
                from,
                to,
                pageable
        );
        return success("Audit logs retrieved successfully", HttpStatus.OK, PageResponse.fromPage(results));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER_OFFICE')")
    @Operation(summary = "Get audit log detail", description = "Get single audit log by id")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Audit log retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Audit log not found")
    })
    public ApiResponse getAuditLogById(@PathVariable UUID id) {
        return success("Audit log retrieved successfully", HttpStatus.OK, auditLogQueryService.getAuditLogById(id));
    }

    @GetMapping(value = "/export.csv", produces = "text/csv")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER_OFFICE')")
    @Operation(summary = "Export audit logs CSV", description = "Export filtered audit logs to CSV")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Audit logs exported"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ResponseEntity<byte[]> exportAuditLogsCsv(
            @RequestParam(required = false) UUID actorUserId,
            @RequestParam(required = false) String httpMethod,
            @RequestParam(required = false) String endpointPattern,
            @RequestParam(required = false) Integer statusCode,
            @RequestParam(required = false) Boolean success,
            @RequestParam(required = false) UUID targetId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(required = false, defaultValue = "5000") Integer limit
    ) {
        String csv = auditLogQueryService.exportAuditLogsCsv(
                actorUserId,
                httpMethod,
                endpointPattern,
                statusCode,
                success,
                targetId,
                from,
                to,
                limit
        );

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"audit-logs.csv\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(csv.getBytes(StandardCharsets.UTF_8));
    }
}


