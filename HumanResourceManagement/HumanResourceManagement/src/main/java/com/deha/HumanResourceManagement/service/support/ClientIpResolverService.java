package com.deha.HumanResourceManagement.service.support;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ClientIpResolverService {

    private static final String[] IP_HEADERS = {
            "X-Forwarded-For",
            "X-Real-IP",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_CLIENT_IP"
    };

    @Value("${app.security.trusted-proxy-cidrs:127.0.0.1/32,::1/128,10.0.0.0/8,172.16.0.0/12,192.168.0.0/16}")
    private String trustedProxyCidrs;

    public String extractClientIp(HttpServletRequest request) {
        if (request == null) return null;

        String remoteAddr = normalizeIp(request.getRemoteAddr());

        // Chỉ trust forwarded headers khi request đi qua trusted proxy.
        if (shouldTrustForwardedHeaders(remoteAddr)) {
            for (String header : IP_HEADERS) {
                String value = request.getHeader(header);
                if (value == null || value.isBlank()) continue;

                String ip = extractBestClientIp(value);
                if (ip != null) {
                    return ip;
                }
            }
        }

        return remoteAddr;
    }

    private String extractBestClientIp(String rawHeader) {
        if (rawHeader == null || rawHeader.isBlank()) return null;

        List<String> candidates = Arrays.stream(rawHeader.split(","))
                .map(this::normalizeIp)
                .filter(this::isValidIp)
                .collect(Collectors.toList());

        if (candidates.isEmpty()) {
            return null;
        }

        return candidates.stream()
                .filter(this::isPublicIp)
                .findFirst()
                .orElseGet(() -> candidates.stream()
                        .filter(ip -> !isLikelyContainerBridgeIp(ip))
                        .findFirst()
                        .orElse(candidates.get(0)));
    }

    private boolean shouldTrustForwardedHeaders(String remoteAddr) {
        return isValidIp(remoteAddr) && isTrustedProxy(remoteAddr);
    }

    private boolean isTrustedProxy(String ip) {
        if (!isValidIp(ip)) {
            return false;
        }

        return Arrays.stream(trustedProxyCidrs.split(","))
                .map(String::trim)
                .filter(entry -> !entry.isBlank())
                .anyMatch(entry -> ipMatchesCidr(ip, entry));
    }

    private boolean ipMatchesCidr(String ip, String cidrOrIp) {
        if (cidrOrIp == null || cidrOrIp.isBlank()) {
            return false;
        }

        if (!cidrOrIp.contains("/")) {
            return cidrOrIp.equals(ip);
        }

        String[] parts = cidrOrIp.split("/", 2);
        InetAddress ipAddr = parseInetAddress(ip);
        InetAddress networkAddr = parseInetAddress(parts[0].trim());
        if (ipAddr == null || networkAddr == null) {
            return false;
        }

        byte[] ipBytes = ipAddr.getAddress();
        byte[] networkBytes = networkAddr.getAddress();
        if (ipBytes.length != networkBytes.length) {
            return false;
        }

        int prefixLength;
        try {
            prefixLength = Integer.parseInt(parts[1].trim());
        } catch (NumberFormatException ex) {
            return false;
        }

        int maxPrefix = ipBytes.length * 8;
        if (prefixLength < 0 || prefixLength > maxPrefix) {
            return false;
        }

        int fullBytes = prefixLength / 8;
        int remainingBits = prefixLength % 8;

        for (int i = 0; i < fullBytes; i++) {
            if (ipBytes[i] != networkBytes[i]) {
                return false;
            }
        }

        if (remainingBits == 0) {
            return true;
        }

        int mask = (0xFF << (8 - remainingBits)) & 0xFF;
        return (ipBytes[fullBytes] & mask) == (networkBytes[fullBytes] & mask);
    }

    private boolean isLikelyContainerBridgeIp(String ip) {
        InetAddress address = parseInetAddress(ip);
        if (!(address instanceof Inet4Address ipv4)) {
            return false;
        }

        byte[] bytes = ipv4.getAddress();
        int first = bytes[0] & 0xFF;
        int second = bytes[1] & 0xFF;

        // Docker bridge thường nằm ở 172.17-172.31.
        return first == 172 && second >= 17 && second <= 31;
    }

    private boolean isPublicIp(String ip) {
        InetAddress address = parseInetAddress(ip);
        if (address == null) {
            return false;
        }

        if (address.isAnyLocalAddress() || address.isLoopbackAddress() || address.isLinkLocalAddress()
                || address.isSiteLocalAddress() || address.isMulticastAddress()) {
            return false;
        }

        return !(address instanceof Inet6Address ipv6) || !isUniqueLocalIpv6(ipv6);
    }

    private boolean isUniqueLocalIpv6(Inet6Address ipv6) {
        byte firstByte = ipv6.getAddress()[0];
        return (firstByte & (byte) 0xFE) == (byte) 0xFC;
    }

    private boolean isValidIp(String ip) {
        return parseInetAddress(ip) != null;
    }

    private InetAddress parseInetAddress(String ip) {
        if (ip == null || ip.isBlank()) {
            return null;
        }

        if (!looksLikeIpLiteral(ip)) {
            return null;
        }

        try {
            return InetAddress.getByName(ip);
        } catch (UnknownHostException ex) {
            return null;
        }
    }

    private boolean looksLikeIpLiteral(String value) {
        if (value == null || value.isBlank()) {
            return false;
        }

        if (!(value.contains(".") || value.contains(":"))) {
            return false;
        }

        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            boolean ok = (ch >= '0' && ch <= '9')
                    || (ch >= 'a' && ch <= 'f')
                    || (ch >= 'A' && ch <= 'F')
                    || ch == '.'
                    || ch == ':'
                    || ch == '%';
            if (!ok) {
                return false;
            }
        }

        return true;
    }

    private String normalizeIp(String raw) {
        if (raw == null) {
            return null;
        }

        String ip = raw.trim();
        if (ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            return null;
        }

        if (ip.startsWith("[") && ip.contains("]")) {
            ip = ip.substring(1, ip.indexOf(']'));
        } else if (ip.chars().filter(ch -> ch == ':').count() == 1 && ip.contains(".")) {
            ip = ip.substring(0, ip.lastIndexOf(':'));
        }

        int zoneIndex = ip.indexOf('%');
        if (zoneIndex > 0) {
            ip = ip.substring(0, zoneIndex);
        }

        return ip;
    }
}