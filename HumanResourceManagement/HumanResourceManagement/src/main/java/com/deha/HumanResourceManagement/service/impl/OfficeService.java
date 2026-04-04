package com.deha.HumanResourceManagement.service.impl;

import com.deha.HumanResourceManagement.dto.office.OfficePolicyRequest;
import com.deha.HumanResourceManagement.dto.office.OfficePolicyResponse;
import com.deha.HumanResourceManagement.dto.office.OfficeRequest;
import com.deha.HumanResourceManagement.dto.office.OfficeResponse;
import com.deha.HumanResourceManagement.entity.Office;
import com.deha.HumanResourceManagement.entity.OfficeWifiIp;
import com.deha.HumanResourceManagement.entity.User;
import com.deha.HumanResourceManagement.exception.BadRequestException;
import com.deha.HumanResourceManagement.exception.ConflictException;
import com.deha.HumanResourceManagement.exception.ForbiddenException;
import com.deha.HumanResourceManagement.exception.ResourceNotFoundException;
import com.deha.HumanResourceManagement.repository.DepartmentRepository;
import com.deha.HumanResourceManagement.repository.OfficeRepository;
import com.deha.HumanResourceManagement.repository.UserRepository;
import com.deha.HumanResourceManagement.service.IOfficeService;
import com.deha.HumanResourceManagement.config.security.AccessScopeService;
import com.deha.HumanResourceManagement.service.support.OfficePolicyService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
    @PersistenceContext
    private EntityManager entityManager;

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
        User actor = accessScopeService.currentUserOrThrow();
        if (!accessScopeService.isAdmin(actor)) {
            throw new ForbiddenException("Only admin can create office");
        }
        if (officeRepository.existsByNameIgnoreCase(request.getName().trim())) {
            throw new ConflictException("Office with the same name already exists");
        }

        List<String> normalizedIps = normalizeIpList(request.getIpWifiIps());
        if (normalizedIps.isEmpty()) {
            throw new BadRequestException("Office must have at least 1 WiFi IP");
        }

        Office office = new Office();
        office.applyDetails(request.getName().trim(), request.getDescription());

        office.setStandardWorkHours(officePolicyService.standardWorkHours(null));
        office.setOtMinHours(officePolicyService.otMinHours(null));
        office.setLatestCheckoutTime(officePolicyService.latestCheckoutTime(null));
        office.setNightStartTime(officePolicyService.nightStartTime(null));
        office.setNightEndTime(officePolicyService.nightEndTime(null));
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
        User actor = accessScopeService.currentUserOrThrow();
        if (!accessScopeService.isAdmin(actor)) {
            throw new ForbiddenException("Only admin can update office");
        }
        Office current = findById(id);
//        assertExpectedVersion(request.getExpectedVersion(), office.getVersion(), "Office");
        if (request.getExpectedVersion() == null) {
            throw new BadRequestException("Expected version is required");
        }
        String nextName = request.getName().trim();
        if (!nextName.equalsIgnoreCase(current.getName()) && officeRepository.existsByNameIgnoreCase(nextName)) {
            throw new ConflictException("Office with the same name already exists");
        }

        List<String> normalizedIps = normalizeIpList(request.getIpWifiIps());
        if (normalizedIps.isEmpty()) {
            throw new BadRequestException("Office must have at least 1 WiFi IP");
        }

        Office office = buildDetachedOffice(current, request.getExpectedVersion());

        office.applyDetails(nextName, request.getDescription());

        Map<String, UUID> existingWifiIdByIp = current.getWifiIps().stream()
                .collect(Collectors.toMap(
                        w -> normalizeIpKey(w.getIpWifi()),
                        OfficeWifiIp::getId,
                        (a, b) -> a
                ));

        office.getWifiIps().clear();
        Set<String> existingIpKeys = new HashSet<>();
        for (String ip : normalizedIps) {
            String ipKey = normalizeIpKey(ip);
            if (!existingIpKeys.add(ipKey)) {
                continue;
            }
            OfficeWifiIp wifi = new OfficeWifiIp();
            wifi.setId(existingWifiIdByIp.get(ipKey));
            wifi.applyDetails(office, ip);
            office.getWifiIps().add(wifi);
        }

        Office merged = mergeAndFlush(office);
        return OfficeResponse.fromEntity(merged);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        User actor = accessScopeService.currentUserOrThrow();
        if (!accessScopeService.isAdmin(actor)) {
            throw new ForbiddenException("Only admin can delete office");
        }
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
        Office current = findById(actor.getOffice().getId());
//        assertExpectedVersion(request.getExpectedVersion(), office.getVersion(), "Office policy");
        if (request.getExpectedVersion() == null) {
            throw new BadRequestException("Expected version is required");
        }
        Office office = buildDetachedOffice(current, request.getExpectedVersion());
        office.setStandardWorkHours(request.getBaseWorkHoursPerDay());
        office.setOtMinHours(request.getOtMinHours());
        office.setLatestCheckoutTime(request.getLatestCheckoutTime());
        office.setNightStartTime(request.getNightStartTime());
        office.setNightEndTime(request.getNightEndTime());
        office.setOtWeekdayMultiplier(request.getOtWeekdayMultiplier());
        office.setOtWeekendMultiplier(request.getOtWeekendMultiplier());
        office.setOtHolidayMultiplier(request.getOtHolidayMultiplier());
        office.setOtNightBonusMultiplier(request.getOtNightBonusMultiplier());
        Office merged = mergeAndFlush(office);
        return OfficePolicyResponse.fromEntity(merged);
    }

//    private void assertExpectedVersion(Long expectedVersion, Long currentVersion, String resourceName) {
//        if (expectedVersion == null) {
//            throw new BadRequestException("Expected version is required");
//        }
//        if (!Objects.equals(expectedVersion, currentVersion)) {
//            throw new ConflictException(resourceName + " was modified by another user. Please refresh and retry.");
//        }
//    }

    private Office buildDetachedOffice(Office current, Long expectedVersion) {
        Office office = new Office();
        office.setId(current.getId());
        office.setVersion(expectedVersion);
        office.setName(current.getName());
        office.setDescription(current.getDescription());
        office.setStandardWorkHours(current.getStandardWorkHours());
        office.setOtMinHours(current.getOtMinHours());
        office.setLatestCheckoutTime(current.getLatestCheckoutTime());
        office.setNightStartTime(current.getNightStartTime());
        office.setNightEndTime(current.getNightEndTime());
        office.setOtWeekdayMultiplier(current.getOtWeekdayMultiplier());
        office.setOtWeekendMultiplier(current.getOtWeekendMultiplier());
        office.setOtHolidayMultiplier(current.getOtHolidayMultiplier());
        office.setOtNightBonusMultiplier(current.getOtNightBonusMultiplier());

        for (OfficeWifiIp existing : current.getWifiIps()) {
            OfficeWifiIp wifi = new OfficeWifiIp();
            wifi.setId(existing.getId());
            wifi.applyDetails(office, existing.getIpWifi());
            office.getWifiIps().add(wifi);
        }
        return office;
    }

    private Office mergeAndFlush(Office office) {
        if (entityManager != null) {
            Office merged = entityManager.merge(office);
            entityManager.flush();
            return merged;
        }
        return officeRepository.saveAndFlush(office);
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

