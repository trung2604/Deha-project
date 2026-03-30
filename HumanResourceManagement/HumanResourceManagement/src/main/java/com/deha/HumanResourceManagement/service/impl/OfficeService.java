package com.deha.HumanResourceManagement.service.impl;

import com.deha.HumanResourceManagement.dto.office.OfficePolicyRequest;
import com.deha.HumanResourceManagement.dto.office.OfficePolicyResponse;
import com.deha.HumanResourceManagement.dto.office.OfficeRequest;
import com.deha.HumanResourceManagement.dto.office.OfficeResponse;
import com.deha.HumanResourceManagement.entity.Office;
import com.deha.HumanResourceManagement.entity.OfficeWifiIp;
import com.deha.HumanResourceManagement.entity.User;
import com.deha.HumanResourceManagement.exception.ConflictException;
import com.deha.HumanResourceManagement.exception.ForbiddenException;
import com.deha.HumanResourceManagement.exception.ResourceNotFoundException;
import com.deha.HumanResourceManagement.repository.DepartmentRepository;
import com.deha.HumanResourceManagement.repository.OfficeRepository;
import com.deha.HumanResourceManagement.repository.UserRepository;
import com.deha.HumanResourceManagement.service.IOfficeService;
import com.deha.HumanResourceManagement.service.support.AccessScopeService;
import com.deha.HumanResourceManagement.service.support.OfficePolicyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OfficeService implements IOfficeService {
    private final OfficeRepository officeRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final AccessScopeService accessScopeService;
    private final OfficePolicyService officePolicyService;

    public OfficeService(
            OfficeRepository officeRepository,
            DepartmentRepository departmentRepository,
            UserRepository userRepository,
            AccessScopeService accessScopeService,
            OfficePolicyService officePolicyService
    ) {
        this.officeRepository = officeRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
        this.accessScopeService = accessScopeService;
        this.officePolicyService = officePolicyService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<OfficeResponse> getAll() {
        return officeRepository.findAll().stream().map(OfficeResponse::fromEntity).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Office findById(UUID id) {
        return officeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Office not found with id: " + id));
    }

    @Override
    @Transactional
    public OfficeResponse create(OfficeRequest request) {
        if (officeRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw new ConflictException("Office with the same name already exists");
        }

        List<String> normalizedIps = normalizeIpList(request.getIpWifiIps());
        if (normalizedIps.isEmpty()) {
            throw new ConflictException("Office must have at least 1 WiFi IP");
        }

        Office office = new Office();
        office.applyDetails(request.getName().trim(), request.getDescription());
        office.setStandardWorkHours(officePolicyService.standardWorkHours(null));
        office.setOtMinHours(officePolicyService.otMinHours(null));
        office.setLatestCheckoutTime(officePolicyService.latestCheckoutTime(null));
        office.setOtWeekdayMultiplier(officePolicyService.otWeekdayMultiplier(null));
        office.setOtWeekendMultiplier(officePolicyService.otWeekendMultiplier(null));
        office.setOtHolidayMultiplier(officePolicyService.otHolidayMultiplier(null));
        office.setOtNightBonusMultiplier(officePolicyService.otNightBonusMultiplier(null));
        for (String ip : normalizedIps) {
            OfficeWifiIp wifi = new OfficeWifiIp();
            wifi.applyDetails(office, ip);
            office.getWifiIps().add(wifi);
        }

        officeRepository.save(office);
        return OfficeResponse.fromEntity(office);
    }

    @Override
    @Transactional
    public OfficeResponse update(UUID id, OfficeRequest request) {
        Office office = findById(id);
        String nextName = request.getName().trim();
        if (!nextName.equalsIgnoreCase(office.getName()) && officeRepository.existsByNameIgnoreCase(nextName)) {
            throw new ConflictException("Office with the same name already exists");
        }

        List<String> normalizedIps = normalizeIpList(request.getIpWifiIps());
        if (normalizedIps.isEmpty()) {
            throw new ConflictException("Office must have at least 1 WiFi IP");
        }

        office.applyDetails(nextName, request.getDescription());

        // Sync by diff to avoid transient unique-key conflicts on (office_id, ip_wifi)
        // that can happen with clear()+re-add in the same persistence context.
        Set<String> targetIpKeys = normalizedIps.stream()
                .map(this::normalizeIpKey)
                .collect(Collectors.toSet());

        office.getWifiIps().removeIf(w -> !targetIpKeys.contains(normalizeIpKey(w.getIpWifi())));

        Set<String> existingIpKeys = office.getWifiIps().stream()
                .map(w -> normalizeIpKey(w.getIpWifi()))
                .collect(Collectors.toCollection(HashSet::new));

        for (String ip : normalizedIps) {
            String ipKey = normalizeIpKey(ip);
            if (existingIpKeys.contains(ipKey)) {
                continue;
            }
            OfficeWifiIp wifi = new OfficeWifiIp();
            wifi.applyDetails(office, ip);
            office.getWifiIps().add(wifi);
            existingIpKeys.add(ipKey);
        }

        officeRepository.save(office);
        return OfficeResponse.fromEntity(office);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        Office office = findById(id);
        long departments = departmentRepository.countByOffice_Id(id);
        if (departments > 0) {
            throw new ConflictException("Cannot delete office while it still has departments");
        }
        long users = userRepository.countByOffice_Id(id);
        if (users > 0) {
            throw new ConflictException("Cannot delete office while it still has users assigned");
        }
        officeRepository.delete(office);
    }

    @Override
    @Transactional(readOnly = true)
    public OfficePolicyResponse getMyPolicy() {
        User actor = accessScopeService.currentUserOrThrow();
        if (!accessScopeService.isOfficeManager(actor)) {
            throw new ForbiddenException("Only office manager can access office policy");
        }
        if (actor.getOffice() == null || actor.getOffice().getId() == null) {
            throw new ResourceNotFoundException("Office policy not found for current user");
        }
        Office office = findById(actor.getOffice().getId());
        return OfficePolicyResponse.fromEntity(office);
    }

    @Override
    @Transactional
    public OfficePolicyResponse updateMyPolicy(OfficePolicyRequest request) {
        User actor = accessScopeService.currentUserOrThrow();
        if (!accessScopeService.isOfficeManager(actor)) {
            throw new ForbiddenException("Only office manager can update office policy");
        }
        if (actor.getOffice() == null || actor.getOffice().getId() == null) {
            throw new ResourceNotFoundException("Office policy not found for current user");
        }
        Office office = findById(actor.getOffice().getId());
        office.setStandardWorkHours(request.getBaseWorkHoursPerDay());
        office.setOtMinHours(request.getOtMinHours());
        office.setLatestCheckoutTime(request.getLatestCheckoutTime());
        office.setOtWeekdayMultiplier(request.getOtWeekdayMultiplier());
        office.setOtWeekendMultiplier(request.getOtWeekendMultiplier());
        office.setOtHolidayMultiplier(request.getOtHolidayMultiplier());
        office.setOtNightBonusMultiplier(request.getOtNightBonusMultiplier());
        officeRepository.save(office);
        return OfficePolicyResponse.fromEntity(office);
    }

    private List<String> normalizeIpList(List<String> ips) {
        if (ips == null) return List.of();
        return ips.stream()
                .map(s -> s == null ? "" : s.trim())
                .filter(s -> !s.isBlank())
                .collect(Collectors.toMap(
                        s -> s.toLowerCase(Locale.ROOT),
                        s -> s,
                        (a, b) -> a
                ))
                .values()
                .stream()
                .toList();
    }

    private String normalizeIpKey(String ip) {
        return ip == null ? "" : ip.trim().toLowerCase(Locale.ROOT);
    }

}

