package com.deha.HumanResourceManagement.service.impl;

import com.deha.HumanResourceManagement.entity.AttendanceLog;
import com.deha.HumanResourceManagement.entity.Office;
import com.deha.HumanResourceManagement.entity.OfficeWifiIp;
import com.deha.HumanResourceManagement.entity.User;
import com.deha.HumanResourceManagement.entity.enums.CheckoutSource;
import com.deha.HumanResourceManagement.exception.BadRequestException;
import com.deha.HumanResourceManagement.exception.UnauthorizedException;
import com.deha.HumanResourceManagement.repository.AttendanceLogRepository;
import com.deha.HumanResourceManagement.repository.OfficeWifiIpRepository;
import com.deha.HumanResourceManagement.service.IAttendanceService;
import com.deha.HumanResourceManagement.config.security.AccessScopeService;
import com.deha.HumanResourceManagement.service.support.OfficePolicyService;
import com.deha.HumanResourceManagement.strategy.OtTypeResolver;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AttendanceService implements IAttendanceService {
    private final AttendanceLogRepository attendanceLogRepo;
    private final OfficeWifiIpRepository wifiIpRepo;
    private final OtTypeResolver otTypeResolver;
    private final AccessScopeService accessScopeService;
    private final OfficePolicyService officePolicyService;

    public AttendanceService(
            AttendanceLogRepository attendanceLogRepo,
            OfficeWifiIpRepository wifiIpRepo,
            OtTypeResolver otTypeResolver,
            AccessScopeService accessScopeService,
            OfficePolicyService officePolicyService
    ) {
        this.attendanceLogRepo = attendanceLogRepo;
        this.wifiIpRepo = wifiIpRepo;
        this.otTypeResolver = otTypeResolver;
        this.accessScopeService = accessScopeService;
        this.officePolicyService = officePolicyService;
    }

    @Override
    @Transactional
    public void checkIn(User user, List<String> clientIps) {
        Office office = user.getOffice();
        if (office == null)
            throw new BadRequestException("User not assigned to any office");

        String matchedIp = matchAllowedIp(office, clientIps);

        if (attendanceLogRepo.existsByUserAndLogDate(user, LocalDate.now()))
            throw new BadRequestException("Already checked in today");

        AttendanceLog log = new AttendanceLog();
        log.checkIn(user, office, matchedIp);
        try {
            attendanceLogRepo.save(log);
        } catch (DataIntegrityViolationException e) {
            throw new BadRequestException("Already checked in today");
        }
    }

    @Override
    @Transactional
    public void checkOut(User user, List<String> clientIps) {
        AttendanceLog log = attendanceLogRepo
                .findByUserAndLogDateAndCheckOutTimeIsNull(user, LocalDate.now())
                .orElseThrow(() -> new BadRequestException("No check-in record found for today"));

        matchAllowedIp(log.getOffice(), clientIps);
        if (log.getCheckInTime() == null) {
            throw new BadRequestException("Not checked in yet");
        }
        if (log.getCheckOutTime() != null) {
            throw new BadRequestException("Already checked out");
        }
        LocalDateTime now = LocalDateTime.now();
        LocalTime latestCheckoutTime = officePolicyService.latestCheckoutTime(log.getOffice());
        LocalDateTime latestCheckoutAt = log.getLogDate().atTime(latestCheckoutTime);
        LocalDateTime effectiveCheckout = now.isAfter(latestCheckoutAt) ? latestCheckoutAt : now;
        if (!effectiveCheckout.isAfter(log.getCheckInTime())) {
            throw new BadRequestException("Invalid checkout time by office policy");
        }
        log.setCheckOutTime(effectiveCheckout);
        log.setCheckoutSource(CheckoutSource.MANUAL);
        log.setAutoCheckedOut(false);
        synchronizeDerivedFields(log);

        attendanceLogRepo.save(log);
    }

    public int calculateTotalWorkedHours(LocalDateTime checkInTime, LocalDateTime checkOutTime) {
        long workedMinutes = Duration.between(checkInTime, checkOutTime).toMinutes();
        if (workedMinutes <= 0) {
            return 0;
        }
        return (int) (workedMinutes / 60);
    }

    @Override
    @Transactional(readOnly = true)
    public int calculateHoursUntil(User user, LocalDate logDate, LocalTime deadlineTime) {
        if (user == null || logDate == null || deadlineTime == null) {
            return 0;
        }
        AttendanceLog log = attendanceLogRepo.findByUserAndLogDate(user, logDate).orElse(null);
        if (log == null || log.getCheckInTime() == null) {
            return 0;
        }
        LocalDateTime deadline = logDate.atTime(deadlineTime);
        if (!log.getCheckInTime().isBefore(deadline)) {
            return 0;
        }
        LocalDateTime effectiveCheckout = log.getCheckOutTime() == null || log.getCheckOutTime().isAfter(deadline)
                ? deadline
                : log.getCheckOutTime();
        return calculateTotalWorkedHours(log.getCheckInTime(), effectiveCheckout);
    }

    @Override
    public boolean synchronizeDerivedFields(AttendanceLog log) {
        if (log == null || log.getCheckInTime() == null) {
            return false;
        }

        Integer newWorkedHours = 0;
        Integer newOtHours = 0;
        if (log.getCheckOutTime() != null) {
            int standardWorkHours = Math.max(1, officePolicyService.standardWorkHours(log.getOffice()));
            int totalWorkedHours = calculateTotalWorkedHours(log.getCheckInTime(), log.getCheckOutTime());
            newWorkedHours = Math.min(totalWorkedHours, standardWorkHours);
            newOtHours = Math.max(0, totalWorkedHours - standardWorkHours);
        }

        boolean changed = !Objects.equals(log.getWorkedHours(), newWorkedHours)
                || !Objects.equals(log.getOtHours(), newOtHours)
                || (newOtHours > 0 && log.getOtType() != otTypeResolver.resolve(log.getLogDate(), log.getCheckOutTime().toLocalTime(), log.getOffice()))
                || (newOtHours == 0 && log.getOtType() != null);

        if (!changed) {
            return false;
        }

        log.setWorkedHours(newWorkedHours);
        log.setOtHours(newOtHours);
        if (newOtHours > 0) {
            log.setOtType(otTypeResolver.resolve(log.getLogDate(), log.getCheckOutTime().toLocalTime(), log.getOffice()));
        } else {
            log.setOtType(null);
        }
        return true;
    }

    @Override
    @Transactional
    public int autoCheckout(LocalDate date) {
        List<AttendanceLog> openLogs = attendanceLogRepo.findByLogDateAndCheckOutTimeIsNull(date);
        if (openLogs.isEmpty()) return 0;
        LocalDateTime now = LocalDateTime.now();
        int changed = 0;
        for (AttendanceLog log : openLogs) {
            LocalTime latestCheckoutTime = officePolicyService.latestCheckoutTime(log.getOffice());
            LocalDateTime autoCheckoutAt = date.atTime(latestCheckoutTime);
            if (now.isBefore(autoCheckoutAt)) {
                continue;
            }
            log.setCheckOutTime(autoCheckoutAt);
            log.setCheckoutSource(CheckoutSource.AUTO);
            log.setAutoCheckedOut(true);
            synchronizeDerivedFields(log);
            attendanceLogRepo.save(log);
            changed++;
        }
        return changed;
    }

    @Override
    @Transactional
    public AttendanceLog getTodayLogOrNull(User user) {
        AttendanceLog log = attendanceLogRepo.findByUserAndLogDate(user, LocalDate.now()).orElse(null);
        if (log == null) {
            return null;
        }
        if (synchronizeDerivedFields(log)) {
            attendanceLogRepo.save(log);
        }
        return log;
    }

    @Override
    @Transactional
    public List<AttendanceLog> getDepartmentTodayLogsOrEmpty(User actor) {
        if (actor == null) {
            throw new UnauthorizedException("Missing authentication context");
        }
        UUID departmentId = actor.getDepartment() != null ? actor.getDepartment().getId() : null;
        accessScopeService.assertCanManageDepartment(departmentId);

        List<AttendanceLog> logs = attendanceLogRepo.findByLogDateAndUser_Department_Id(LocalDate.now(), departmentId);
        for (AttendanceLog log : logs) {
            if (synchronizeDerivedFields(log)) {
                attendanceLogRepo.save(log);
            }
        }
        return logs;
    }

    @Override
    @Transactional(readOnly = true)
    public void validateOfficeIpAccess(Office office, List<String> clientIps) {
        matchAllowedIp(office, clientIps);
    }

    private String matchAllowedIp(Office office, List<String> clientIps) {
        Set<String> candidateIps = (clientIps == null ? List.<String>of() : clientIps).stream()
                .map(this::normalizeIp)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
        if (candidateIps.stream().anyMatch(this::isLoopback)) {
            candidateIps.add("127.0.0.1");
            candidateIps.add("::1");
            candidateIps.addAll(resolveLocalLanIpv4s());
        }

        if (candidateIps.isEmpty()) {
            throw new BadRequestException("Client IP is required");
        }

        Set<String> allowedIps = wifiIpRepo.findByOffice(office).stream()
                .map(OfficeWifiIp::getIpWifi)
                .map(this::normalizeIp)
                .filter(s -> !s.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        if (allowedIps.isEmpty()) {
            throw new BadRequestException("Office does not have any registered Wi-Fi IPs");
        }

        for (String candidate : candidateIps) {
            for (String allowed : allowedIps) {
                if (candidate.equals(allowed)) {
                    return candidate;
                }
                if (isLoopback(candidate) && isLoopback(allowed)) {
                    return candidate;
                }
            }
        }

        throw new BadRequestException(
                "Client IP is not within allowed Wi-Fi IPs for the office. Detected IPs: " + String.join(", ", candidateIps)
        );
    }

    private String normalizeIp(String ip) {
        if (ip == null) return "";
        String t = ip.trim();
        if (t.isBlank()) return "";

        if (t.startsWith("::ffff:")) {
            t = t.substring("::ffff:".length());
        }

        if (t.contains(".") && t.contains(":")) {
            t = t.substring(0, t.lastIndexOf(':'));
        }

        if (t.startsWith("[") && t.endsWith("]") && t.length() > 2) {
            t = t.substring(1, t.length() - 1);
        }

        return t.toLowerCase();
    }

    private boolean isLoopback(String ip) {
        String value = normalizeIp(ip);
        return "127.0.0.1".equals(value) || "::1".equals(value) || "0:0:0:0:0:0:0:1".equals(value);
    }

    private Set<String> resolveLocalLanIpv4s() {
        Set<String> result = new LinkedHashSet<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces != null && interfaces.hasMoreElements()) {
                NetworkInterface networkInterface = interfaces.nextElement();
                if (!networkInterface.isUp() || networkInterface.isLoopback() || networkInterface.isVirtual()) {
                    continue;
                }

                Enumeration<InetAddress> addresses = networkInterface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress inetAddress = addresses.nextElement();
                    if (inetAddress instanceof Inet4Address && !inetAddress.isLoopbackAddress()) {
                        result.add(normalizeIp(inetAddress.getHostAddress()));
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return result;
    }
}


