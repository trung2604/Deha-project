package com.deha.HumanResourceManagement.service;

import com.deha.HumanResourceManagement.dto.office.OfficeRequest;
import com.deha.HumanResourceManagement.dto.office.OfficeResponse;
import com.deha.HumanResourceManagement.entity.Office;
import com.deha.HumanResourceManagement.entity.OfficeWifiIp;
import com.deha.HumanResourceManagement.exception.ConflictException;
import com.deha.HumanResourceManagement.exception.ResourceNotFoundException;
import com.deha.HumanResourceManagement.repository.DepartmentRepository;
import com.deha.HumanResourceManagement.repository.OfficeRepository;
import com.deha.HumanResourceManagement.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OfficeService {
    private final OfficeRepository officeRepository;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;

    public OfficeService(OfficeRepository officeRepository, DepartmentRepository departmentRepository, UserRepository userRepository) {
        this.officeRepository = officeRepository;
        this.departmentRepository = departmentRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<OfficeResponse> getAll() {
        return officeRepository.findAll().stream().map(OfficeResponse::fromEntity).toList();
    }

    @Transactional(readOnly = true)
    public Office findById(UUID id) {
        return officeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Office not found with id: " + id));
    }

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
        for (String ip : normalizedIps) {
            OfficeWifiIp wifi = new OfficeWifiIp();
            wifi.applyDetails(office, ip);
            office.getWifiIps().add(wifi);
        }

        officeRepository.save(office);
        return OfficeResponse.fromEntity(office);
    }

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
        office.getWifiIps().clear();
        for (String ip : normalizedIps) {
            OfficeWifiIp wifi = new OfficeWifiIp();
            wifi.applyDetails(office, ip);
            office.getWifiIps().add(wifi);
        }

        officeRepository.save(office);
        return OfficeResponse.fromEntity(office);
    }

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
}
