package com.deha.HumanResourceManagement.service.support;

import com.deha.HumanResourceManagement.dto.audit.AuditLogResponse;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AuditLogCsvExporter {

    public String toCsv(List<AuditLogResponse> rows) {
        StringBuilder sb = new StringBuilder();
        sb.append("id,occurredAt,actorUserId,actorOfficeId,actorEmail,httpMethod,endpointPattern,requestUri,targetId,statusCode,success,clientIp,userAgent,durationMs\n");
        for (AuditLogResponse row : rows) {
            appendCell(sb, row.getId());
            appendCell(sb, row.getOccurredAt());
            appendCell(sb, row.getActorUserId());
            appendCell(sb, row.getActorOfficeId());
            appendCell(sb, row.getActorEmail());
            appendCell(sb, row.getHttpMethod());
            appendCell(sb, row.getEndpointPattern());
            appendCell(sb, row.getRequestUri());
            appendCell(sb, row.getTargetId());
            appendCell(sb, row.getStatusCode());
            appendCell(sb, row.getSuccess());
            appendCell(sb, row.getClientIp());
            appendCell(sb, row.getUserAgent());
            appendCell(sb, row.getDurationMs());
            sb.append('\n');
        }
        return sb.toString();
    }

    private void appendCell(StringBuilder sb, Object value) {
        if (!sb.isEmpty() && sb.charAt(sb.length() - 1) != '\n') {
            sb.append(',');
        }
        if (value == null) {
            return;
        }
        String text = value.toString();
        boolean needsQuote = text.contains(",") || text.contains("\"") || text.contains("\n") || text.contains("\r");
        if (!needsQuote) {
            sb.append(text);
            return;
        }
        sb.append('"');
        sb.append(text.replace("\"", "\"\""));
        sb.append('"');
    }
}

