package com.deha.HumanResourceManagement.service.impl;

import com.deha.HumanResourceManagement.config.security.AccessScopeService;
import com.deha.HumanResourceManagement.dto.salarycontract.SalaryContractRequest;
import com.deha.HumanResourceManagement.dto.salarycontract.SalaryContractResponse;
import com.deha.HumanResourceManagement.entity.SalaryContract;
import com.deha.HumanResourceManagement.entity.User;
import com.deha.HumanResourceManagement.exception.BadRequestException;
import com.deha.HumanResourceManagement.exception.ResourceNotFoundException;
import com.deha.HumanResourceManagement.mapper.salarycontract.SalaryContractMapper;
import com.deha.HumanResourceManagement.repository.SalaryContractRepository;
import com.deha.HumanResourceManagement.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class SalaryContractService {
    private final SalaryContractRepository salaryContractRepository;
    private final UserRepository userRepository;
    private final AccessScopeService accessScopeService;
    private final SalaryContractMapper salaryContractMapper;
    @PersistenceContext
    private EntityManager entityManager;

    public SalaryContractService(
            SalaryContractRepository salaryContractRepository,
            UserRepository userRepository,
            AccessScopeService accessScopeService,
            SalaryContractMapper salaryContractMapper
    ) {
        this.salaryContractRepository = salaryContractRepository;
        this.userRepository = userRepository;
        this.accessScopeService = accessScopeService;
        this.salaryContractMapper = salaryContractMapper;
    }

    @Transactional
    public SalaryContractResponse create(SalaryContractRequest request) {
        User user = userOrThrow(request.getUserId());
        assertScope(user);
        validateDateRange(request.getStartDate(), request.getEndDate());
        ensureNoOverlap(user, request.getStartDate(), request.getEndDate(), null);

        SalaryContract contract = new SalaryContract();
        contract.setUser(user);
        contract.setBaseSalary(request.getBaseSalary());
        contract.setStartDate(request.getStartDate());
        contract.setEndDate(request.getEndDate());
        salaryContractRepository.save(contract);
        return salaryContractMapper.toResponse(contract);
    }

    @Transactional
    public SalaryContractResponse update(UUID id, SalaryContractRequest request) {
        SalaryContract current = salaryContractRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Salary contract not found"));
//        assertExpectedVersion(request.getExpectedVersion(), contract.getVersion(), "Salary contract");
        if (request.getExpectedVersion() == null) {
            throw new BadRequestException("Expected version is required");
        }

        User user = userOrThrow(request.getUserId());
        assertScope(user);
        validateDateRange(request.getStartDate(), request.getEndDate());
        ensureNoOverlap(user, request.getStartDate(), request.getEndDate(), current.getId());

        SalaryContract contract = new SalaryContract();
        contract.setId(current.getId());
        contract.setVersion(request.getExpectedVersion());
        contract.setUser(user);
        contract.setBaseSalary(request.getBaseSalary());
        contract.setStartDate(request.getStartDate());
        contract.setEndDate(request.getEndDate());
        SalaryContract merged = mergeAndFlush(contract);
        return salaryContractMapper.toResponse(merged);
    }

//    private void assertExpectedVersion(Long expectedVersion, Long currentVersion, String resourceName) {
//        if (expectedVersion == null) {
//            throw new BadRequestException("Expected version is required");
//        }
//        if (!Objects.equals(expectedVersion, currentVersion)) {
//            throw new ConflictException(resourceName + " was modified by another user. Please refresh and retry.");
//        }
//    }

    @Transactional(readOnly = true)
    public List<SalaryContractResponse> getByUser(UUID userId) {
        User user = userOrThrow(userId);
        assertScope(user);
        return salaryContractRepository.findByUserOrderByStartDateDesc(user)
                .stream()
                .map(salaryContractMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SalaryContract findActiveContractOrNull(User user, LocalDate periodStart, LocalDate periodEnd) {
        return salaryContractRepository.findByUserOrderByStartDateDesc(user).stream()
                .filter(c -> c.overlaps(periodStart, periodEnd))
                .findFirst()
                .orElse(null);
    }

    private User userOrThrow(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void assertScope(User user) {
        UUID officeId = user.getOffice() != null ? user.getOffice().getId() : null;
        accessScopeService.assertCanManageOffice(officeId);
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null) {
            throw new BadRequestException("Start date is required");
        }
        if (endDate != null && endDate.isBefore(startDate)) {
            throw new BadRequestException("End date must be greater than or equal to start date");
        }
    }

    private void ensureNoOverlap(User user, LocalDate startDate, LocalDate endDate, UUID selfIdToIgnore) {
        LocalDate maxEnd = LocalDate.of(9999, 12, 31);
        LocalDate requestEnd = endDate == null ? maxEnd : endDate;

        boolean overlapped = salaryContractRepository.findByUserOrderByStartDateDesc(user).stream()
                .filter(c -> selfIdToIgnore == null || !Objects.equals(c.getId(), selfIdToIgnore))
                .anyMatch(c -> {
                    LocalDate cEnd = c.getEndDate() == null ? maxEnd : c.getEndDate();
                    return !c.getStartDate().isAfter(requestEnd) && !cEnd.isBefore(startDate);
                });

        if (overlapped) {
            throw new BadRequestException("Salary contract date range overlaps with an existing contract");
        }
    }

    private SalaryContract mergeAndFlush(SalaryContract contract) {
        if (entityManager != null) {
            SalaryContract merged = entityManager.merge(contract);
            entityManager.flush();
            return merged;
        }
        return salaryContractRepository.saveAndFlush(contract);
    }
}


