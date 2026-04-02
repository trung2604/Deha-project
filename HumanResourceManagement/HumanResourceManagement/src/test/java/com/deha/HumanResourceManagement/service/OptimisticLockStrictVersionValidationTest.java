package com.deha.HumanResourceManagement.service;

import com.deha.HumanResourceManagement.dto.department.DepartmentRequest;
import com.deha.HumanResourceManagement.dto.office.OfficeRequest;
import com.deha.HumanResourceManagement.dto.position.PositionRequest;
import com.deha.HumanResourceManagement.dto.salarycontract.SalaryContractRequest;
import com.deha.HumanResourceManagement.entity.Department;
import com.deha.HumanResourceManagement.entity.Office;
import com.deha.HumanResourceManagement.entity.Position;
import com.deha.HumanResourceManagement.entity.SalaryContract;
import com.deha.HumanResourceManagement.exception.BadRequestException;
import com.deha.HumanResourceManagement.exception.ConflictException;
import com.deha.HumanResourceManagement.repository.DepartmentRepository;
import com.deha.HumanResourceManagement.repository.OfficeRepository;
import com.deha.HumanResourceManagement.repository.PositionRepository;
import com.deha.HumanResourceManagement.repository.SalaryContractRepository;
import com.deha.HumanResourceManagement.repository.UserRepository;
import com.deha.HumanResourceManagement.service.impl.DepartmentService;
import com.deha.HumanResourceManagement.service.impl.OfficeService;
import com.deha.HumanResourceManagement.service.impl.PositionService;
import com.deha.HumanResourceManagement.service.impl.SalaryContractService;
import com.deha.HumanResourceManagement.service.support.AccessScopeService;
import com.deha.HumanResourceManagement.service.support.OfficePolicyService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OptimisticLockStrictVersionValidationTest {

    @Test
    void officeUpdate_withoutExpectedVersion_shouldThrowBadRequest() {
        OfficeRepository officeRepository = mock(OfficeRepository.class);
        Office office = new Office();
        office.setId(UUID.randomUUID());
        office.setVersion(2L);
        when(officeRepository.findById(office.getId())).thenReturn(Optional.of(office));

        OfficeService officeService = new OfficeService(
                officeRepository,
                mock(DepartmentRepository.class),
                mock(UserRepository.class),
                new AccessScopeService(null),
                new OfficePolicyService()
        );

        OfficeRequest request = new OfficeRequest();
        request.setName("HQ");
        request.setIpWifiIps(List.of("192.168.1.10"));

        assertThrows(BadRequestException.class, () -> officeService.update(office.getId(), request));
    }

    @Test
    void officeUpdate_withStaleExpectedVersion_shouldThrowConflict() {
        OfficeRepository officeRepository = mock(OfficeRepository.class);
        Office office = new Office();
        office.setId(UUID.randomUUID());
        office.setVersion(2L);
        when(officeRepository.findById(office.getId())).thenReturn(Optional.of(office));

        OfficeService officeService = new OfficeService(
                officeRepository,
                mock(DepartmentRepository.class),
                mock(UserRepository.class),
                new AccessScopeService(null),
                new OfficePolicyService()
        );

        OfficeRequest request = new OfficeRequest();
        request.setExpectedVersion(1L);
        request.setName("HQ");
        request.setIpWifiIps(List.of("192.168.1.10"));

        assertThrows(ConflictException.class, () -> officeService.update(office.getId(), request));
    }

    @Test
    void departmentUpdate_withoutExpectedVersion_shouldThrowBadRequest() {
        DepartmentRepository departmentRepository = mock(DepartmentRepository.class);
        Department department = new Department();
        department.setId(UUID.randomUUID());
        department.setVersion(5L);
        when(departmentRepository.findById(department.getId())).thenReturn(Optional.of(department));

        DepartmentService departmentService = new DepartmentService(
                departmentRepository,
                mock(PositionRepository.class),
                mock(UserRepository.class),
                mock(IOfficeService.class),
                new AccessScopeService(null),
                mock(EntityManager.class)
        );

        DepartmentRequest request = new DepartmentRequest();
        request.setName("Engineering");

        assertThrows(BadRequestException.class, () -> departmentService.updateDepartment(department.getId(), request));
    }

    @Test
    void departmentUpdate_withStaleExpectedVersion_shouldThrowConflict() {
        DepartmentRepository departmentRepository = mock(DepartmentRepository.class);
        Department department = new Department();
        department.setId(UUID.randomUUID());
        department.setVersion(5L);
        when(departmentRepository.findById(department.getId())).thenReturn(Optional.of(department));

        DepartmentService departmentService = new DepartmentService(
                departmentRepository,
                mock(PositionRepository.class),
                mock(UserRepository.class),
                mock(IOfficeService.class),
                new AccessScopeService(null),
                mock(EntityManager.class)
        );

        DepartmentRequest request = new DepartmentRequest();
        request.setExpectedVersion(4L);
        request.setName("Engineering");

        assertThrows(ConflictException.class, () -> departmentService.updateDepartment(department.getId(), request));
    }

    @Test
    void positionUpdate_withoutExpectedVersion_shouldThrowBadRequest() {
        PositionRepository positionRepository = mock(PositionRepository.class);
        Position position = new Position();
        position.setId(UUID.randomUUID());
        position.setVersion(3L);
        when(positionRepository.findById(position.getId())).thenReturn(Optional.of(position));

        PositionService positionService = new PositionService(
                positionRepository,
                mock(IDepartmentService.class),
                mock(DepartmentRepository.class),
                mock(UserRepository.class),
                new AccessScopeService(null)
        );

        PositionRequest request = new PositionRequest();
        request.setName("Senior Engineer");

        assertThrows(BadRequestException.class, () -> positionService.updatePosition(position.getId(), UUID.randomUUID(), request));
    }

    @Test
    void positionUpdate_withStaleExpectedVersion_shouldThrowConflict() {
        PositionRepository positionRepository = mock(PositionRepository.class);
        Position position = new Position();
        position.setId(UUID.randomUUID());
        position.setVersion(3L);
        when(positionRepository.findById(position.getId())).thenReturn(Optional.of(position));

        PositionService positionService = new PositionService(
                positionRepository,
                mock(IDepartmentService.class),
                mock(DepartmentRepository.class),
                mock(UserRepository.class),
                new AccessScopeService(null)
        );

        PositionRequest request = new PositionRequest();
        request.setExpectedVersion(2L);
        request.setName("Senior Engineer");

        assertThrows(ConflictException.class, () -> positionService.updatePosition(position.getId(), UUID.randomUUID(), request));
    }

    @Test
    void salaryContractUpdate_withoutExpectedVersion_shouldThrowBadRequest() {
        SalaryContractRepository salaryContractRepository = mock(SalaryContractRepository.class);
        SalaryContract contract = new SalaryContract();
        contract.setId(UUID.randomUUID());
        contract.setVersion(7L);
        when(salaryContractRepository.findById(contract.getId())).thenReturn(Optional.of(contract));

        SalaryContractService salaryContractService = new SalaryContractService(
                salaryContractRepository,
                mock(UserRepository.class),
                new AccessScopeService(null)
        );

        SalaryContractRequest request = new SalaryContractRequest();

        assertThrows(BadRequestException.class, () -> salaryContractService.update(contract.getId(), request));
    }

    @Test
    void salaryContractUpdate_withStaleExpectedVersion_shouldThrowConflict() {
        SalaryContractRepository salaryContractRepository = mock(SalaryContractRepository.class);
        SalaryContract contract = new SalaryContract();
        contract.setId(UUID.randomUUID());
        contract.setVersion(7L);
        when(salaryContractRepository.findById(contract.getId())).thenReturn(Optional.of(contract));

        SalaryContractService salaryContractService = new SalaryContractService(
                salaryContractRepository,
                mock(UserRepository.class),
                new AccessScopeService(null)
        );

        SalaryContractRequest request = new SalaryContractRequest();
        request.setExpectedVersion(6L);

        assertThrows(ConflictException.class, () -> salaryContractService.update(contract.getId(), request));
    }
}

