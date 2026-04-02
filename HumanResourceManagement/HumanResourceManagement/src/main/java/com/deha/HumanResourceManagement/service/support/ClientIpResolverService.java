package com.deha.HumanResourceManagement.service.support;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class ClientIpResolverService {

    public List<String> extractClientIps(HttpServletRequest request) {
        Set<String> uniqueIps = new LinkedHashSet<>();

        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            for (String part : forwardedFor.split(",")) {
                String value = part == null ? "" : part.trim();
                if (!value.isBlank()) uniqueIps.add(value);
            }
        }

        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            uniqueIps.add(realIp.trim());
        }

        String remoteAddr = request.getRemoteAddr();
        if (remoteAddr != null && !remoteAddr.isBlank()) {
            uniqueIps.add(remoteAddr.trim());
        }

        return new ArrayList<>(uniqueIps);
    }
}



