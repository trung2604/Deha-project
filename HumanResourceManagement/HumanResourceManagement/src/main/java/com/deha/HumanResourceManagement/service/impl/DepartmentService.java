package com.deha.HumanResourceManagement.service.impl;

import com.deha.HumanResourceManagement.dto.department.DepartmentDirectoryResponse;
import com.deha.HumanResourceManagement.dto.department.DepartmentRequest;
import com.deha.HumanResourceManagement.dto.department.DepartmentDetailResponse;
import com.deha.HumanResourceManagement.dto.department.DepartmentResponse;
import com.deha.HumanResourceManagement.entity.Department;
import com.deha.HumanResourceManagement.entity.Office;
import com.deha.HumanResourceManagement.entity.User;
import com.deha.HumanResourceManagement.exception.BadRequestException;
import com.deha.HumanResourceManagement.exception.ForbiddenException;
import com.deha.HumanResourceManagement.exception.ConflictException;
import com.deha.HumanResourceManagement.exception.ResourceAlreadyExistException;
import com.deha.HumanResourceManagement.exception.ResourceNotFoundException;
import com.deha.HumanResourceManagement.repository.DepartmentRepository;
import com.deha.HumanResourceManagement.repository.PositionRepository;
import com.deha.HumanResourceManagement.repository.UserRepository;
import com.deha.HumanResourceManagement.repository.specification.DepartmentSpecification;
import com.deha.HumanResourceManagement.service.IDepartmentService;
import com.deha.HumanResourceManagement.service.IOfficeService;
import com.deha.HumanResourceManagement.config.security.AccessScopeService;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class DepartmentService implements IDepartmentService {
    private final DepartmentRepository departmentRepository;
    private final PositionRepository positionRepository;
    private final UserRepository userRepository;
    private final IOfficeService officeService;
    private final AccessScopeService accessScopeService;
    private final EntityManager entityManager;

    public DepartmentService(
            DepartmentRepository departmentRepository,
            PositionRepository positionRepository,
            UserRepository userRepository,
            IOfficeService officeService,
            AccessScopeService accessScopeService, EntityManager entityManager
    ) {
        this.departmentRepository = departmentRepository;
        this.positionRepository = positionRepository;
        this.userRepository = userRepository;
        this.officeService = officeService;
        this.accessScopeService = accessScopeService;
        this.entityManager = entityManager;
    }

    @Override
    public DepartmentResponse createDepartment(DepartmentRequest departmentRequest){
        Office office = officeService.findById(departmentRequest.getOfficeId());
        accessScopeService.assertCanManageOffice(office.getId());
        if(departmentRepository.existsByNameIgnoreCaseAndOffice_Id(departmentRequest.getName(), office.getId())) {
            throw new ResourceAlreadyExistException("Department with the same name already exists.");
        }
        Department department = new Department();
        department.applyDetails(departmentRequest.getName(), departmentRequest.getDescription());
        department.assignOffice(office);
        departmentRepository.save(department);
        return DepartmentResponse.fromEntity(department);
    }

//    @Override
//    @Transactional
//    public DepartmentResponse updateDepartment(UUID id, DepartmentRequest departmentRequest){
//        Department department = departmentRepository.findById(id).orElseThrow(
//                () -> new ResourceNotFoundException("Department not found with id: " + id));
//        assertExpectedVersion(departmentRequest.getExpectedVersion(), department.getVersion(), "Department");
//        Office office = officeService.findById(departmentRequest.getOfficeId());
//        accessScopeService.assertCanManageOffice(office.getId());
//        boolean changedOffice = department.getOffice() == null || !department.getOffice().getId().equals(office.getId());
//        boolean changedName = department.getName() == null || !department.getName().equalsIgnoreCase(departmentRequest.getName());
//        if ((changedOffice || changedName)
//                && departmentRepository.existsByNameIgnoreCaseAndOffice_Id(departmentRequest.getName(), office.getId())) {
//            throw new ResourceAlreadyExistException("Department with the same name already exists.");
//        }
//        department.applyDetails(departmentRequest.getName(), departmentRequest.getDescription());
//        department.assignOffice(office);
//        departmentRepository.saveAndFlush(department);
//        return DepartmentResponse.fromEntity(department);
//    }
@Override
@Transactional
public DepartmentResponse updateDepartment(UUID id, DepartmentRequest departmentRequest) {
    if (departmentRequest.getExpectedVersion() == null) {
        throw new BadRequestException("Expected version is required");
    }
    Department department = new Department();
    department.setId(id);
    department.setVersion(departmentRequest.getExpectedVersion());
    Office office = officeService.findById(departmentRequest.getOfficeId());
    accessScopeService.assertCanManageOffice(office.getId());
    department.applyDetails(departmentRequest.getName(), departmentRequest.getDescription());
    department.assignOffice(office);
    departmentRepository.saveAndFlush(department);
    return DepartmentResponse.fromEntity(department);
}

//    private void assertExpectedVersion(Long expectedVersion, Long currentVersion, String resourceName) {
//        if (expectedVersion == null) {
//            throw new BadRequestException("Expected version is required");
//        }
//        if (!Objects.equals(expectedVersion, currentVersion)) {
//            throw new ConflictException(resourceName + " was modified by another user. Please refresh and retry.");
//        }
//    }

    @Override
    @Transactional
    public void deleteDepartment(UUID id){
        Department department = departmentRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Department not found with id: " + id));
        accessScopeService.assertCanManageOffice(
                department.getOffice() != null ? department.getOffice().getId() : null
        );
        long usersInDept = userRepository.countByDepartment_Id(id);
        if (usersInDept > 0) {
            throw new ConflictException(
                    "Cannot delete department while it still has users assigned. Reassign or remove users first.");
        }
        // Safe order: no users remain, so positions can be removed without FK violations from users.position_id
        positionRepository.deleteAllByDepartmentId(id);
        departmentRepository.delete(department);
    }

    @Override
    public DepartmentResponse getDepartmentById(UUID id){
        Department department = departmentRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Department not found with id: " + id));
        return DepartmentResponse.fromEntity(department);
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentDetailResponse getDepartmentDetailById(UUID id) {
        Department department = departmentRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Department not found with id: " + id));
        User actor = accessScopeService.currentUserOrThrow();
        if (accessScopeService.isDepartmentManager(actor)) {
            accessScopeService.assertCanManageDepartment(id);
        } else {
            accessScopeService.assertCanManageOffice(
                    department.getOffice() != null ? department.getOffice().getId() : null
            );
        }

        return DepartmentDetailResponse.fromEntity(department);
    }

    @Override
    @Transactional(readOnly = true)
    public DepartmentDirectoryResponse getDepartmentDirectory(String keyword, UUID officeId) {

        User actor = accessScopeService.currentUserOrThrow();
        if (accessScopeService.isDepartmentManager(actor)) {
            UUID scopedDepartmentId = actor.getDepartment() != null ? actor.getDepartment().getId() : null;

            if (scopedDepartmentId == null) {
                throw new ForbiddenException("Department manager must be assigned to a department");
            }

            Department dept = departmentRepository.findById(scopedDepartmentId).orElseThrow(
                    () -> new ResourceNotFoundException("Department not found with id: " + scopedDepartmentId)
            );

            String normalized = (keyword != null && !keyword.isBlank()) ? keyword.trim().toLowerCase() : null;

            boolean matches = normalized == null
                    || (dept.getName() != null && dept.getName().toLowerCase().contains(normalized))
                    || (dept.getDescription() != null && dept.getDescription().toLowerCase().contains(normalized));

            List<DepartmentResponse> list = matches
                    ? List.of(DepartmentResponse.fromEntity(dept))
                    : List.of();

            long totalCount = matches ? 1 : 0;

            return new DepartmentDirectoryResponse(list, totalCount);
        }

        UUID scopedOfficeId = accessScopeService.isAdmin(actor)
                ? officeId
                : (actor.getOffice() != null ? actor.getOffice().getId() : null);

        String normalized = (keyword != null && !keyword.isBlank()) ? keyword.trim() : null;

        Specification<Department> spec = Specification
                .where(DepartmentSpecification.search(normalized))
                .and(DepartmentSpecification.hasOffice(scopedOfficeId));

        List<Department> rows = departmentRepository.findAll(
                spec,
                Sort.by(Sort.Direction.ASC, "name")
        );

        long totalCount = departmentRepository.count(spec);
        List<DepartmentResponse> list = rows.stream()
                .map(DepartmentResponse::fromEntity)
                .toList();

        return new DepartmentDirectoryResponse(list, totalCount);
    }

    @Override
    public Department findDepartmentById(UUID id) {
        Department department = departmentRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Department not found with id: " + id));
        accessScopeService.assertCanManageOffice(
                department.getOffice() != null ? department.getOffice().getId() : null
        );
        return department;
    }
}


