package com.deha.HumanResourceManagement.service.impl;

import com.deha.HumanResourceManagement.dto.position.PositionRequest;
import com.deha.HumanResourceManagement.dto.position.PositionResponse;
import com.deha.HumanResourceManagement.entity.Department;
import com.deha.HumanResourceManagement.entity.Position;
import com.deha.HumanResourceManagement.entity.User;
import com.deha.HumanResourceManagement.exception.BadRequestException;
import com.deha.HumanResourceManagement.exception.ConflictException;
import com.deha.HumanResourceManagement.exception.ResourceAlreadyExistException;
import com.deha.HumanResourceManagement.exception.ResourceNotFoundException;
import com.deha.HumanResourceManagement.repository.DepartmentRepository;
import com.deha.HumanResourceManagement.repository.PositionRepository;
import com.deha.HumanResourceManagement.repository.UserRepository;
import com.deha.HumanResourceManagement.service.IDepartmentService;
import com.deha.HumanResourceManagement.service.IPositionService;
import com.deha.HumanResourceManagement.service.support.AccessScopeService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PositionService implements IPositionService {
    private final PositionRepository positionRepository;
    private final IDepartmentService departmentService;
    private final DepartmentRepository departmentRepository;
    private final UserRepository userRepository;
    private final AccessScopeService accessScopeService;
    @PersistenceContext
    private EntityManager entityManager;

    public PositionService(
            PositionRepository positionRepository,
            IDepartmentService departmentService,
            DepartmentRepository departmentRepository,
            UserRepository userRepository,
            AccessScopeService accessScopeService
    ) {
        this.departmentService = departmentService;
        this.departmentRepository = departmentRepository;
        this.positionRepository = positionRepository;
        this.userRepository = userRepository;
        this.accessScopeService = accessScopeService;
    }

    @Override
    public List<PositionResponse> getAllPositionsOfDepartment(UUID departmentId) {
        Department department = departmentRepository.findById(departmentId).orElseThrow(
                () -> new ResourceNotFoundException("Department not found with id: " + departmentId));
        // Read-only permission:
        // - ADMIN can view any department positions
        // - OFFICE manager can view positions for departments in their office
        // - DEPARTMENT manager can view positions only for their own department
        User actor = accessScopeService.currentUserOrThrow();
        if (accessScopeService.isAdmin(actor)) {
            // no-op
        } else if (accessScopeService.isOfficeManager(actor)) {
            UUID officeId = department.getOffice() != null ? department.getOffice().getId() : null;
            accessScopeService.assertCanManageOffice(officeId);
        } else if (accessScopeService.isDepartmentManager(actor)) {
            accessScopeService.assertCanManageDepartment(departmentId);
        } else {
            // Employees (and any unexpected roles) are not allowed to read department positions.
            accessScopeService.assertCanManageDepartment(departmentId);
        }
        return positionRepository.findAllByDepartmentId(departmentId).stream()
                .map(PositionResponse::fromEntity)
                .toList();
    }

    @Override
    public List<PositionResponse> getAllPositions() {
        User actor = accessScopeService.currentUserOrThrow();
        UUID scopedOfficeId = accessScopeService.isAdmin(actor)
                ? null
                : (actor.getOffice() != null ? actor.getOffice().getId() : null);
        return positionRepository.findAll().stream()
                .filter(position -> scopedOfficeId == null
                        || (position.getDepartment() != null
                        && position.getDepartment().getOffice() != null
                        && scopedOfficeId.equals(position.getDepartment().getOffice().getId())))
                .map(PositionResponse::fromEntity)
                .toList();
    }

    @Override
    public PositionResponse getPositionById(UUID id) {
        Position position = positionRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Position not found with id: " + id));
        UUID officeId = position.getDepartment() != null && position.getDepartment().getOffice() != null
                ? position.getDepartment().getOffice().getId()
                : null;
        accessScopeService.assertCanManageOffice(officeId);
        return PositionResponse.fromEntity(position);
    }

    @Override
    @Transactional
    public PositionResponse createPosition(UUID departmentId, PositionRequest request) {
        Department department = departmentService.findDepartmentById(departmentId);

        if (positionRepository.existsByNameInDepartment(departmentId, request.getName())) {
            throw new ResourceAlreadyExistException("Position with the same name already exists in the department.");
        }

        Position newPosition = new Position();
        newPosition.rename(request.getName());
        newPosition.assignDepartment(department);
        positionRepository.save(newPosition);

        return PositionResponse.fromEntity(newPosition);
    }

    @Override
    @Transactional
    public PositionResponse updatePosition(UUID id, UUID departmentId, PositionRequest request) {
        Position current = positionRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Position not found with id: " + id));
//        assertExpectedVersion(request.getExpectedVersion(), existingPosition.getVersion(), "Position");
        if (request.getExpectedVersion() == null) {
            throw new BadRequestException("Expected version is required");
        }
        UUID existingOfficeId = current.getDepartment() != null && current.getDepartment().getOffice() != null
                ? current.getDepartment().getOffice().getId()
                : null;
        accessScopeService.assertCanManageOffice(existingOfficeId);

        Department department = departmentService.findDepartmentById(departmentId);
        boolean movedDepartment = !current.belongsToDepartment(departmentId);

        boolean nameChanged = current.isNameChanged(request.getName()) || movedDepartment;
        if (nameChanged && positionRepository.existsByNameInDepartment(departmentId, request.getName())) {
            throw new ResourceAlreadyExistException("Position with the same name already exists in the department.");
        }

        Position position = new Position();
        position.setId(current.getId());
        position.setVersion(request.getExpectedVersion());
        position.rename(request.getName());
        position.assignDepartment(department);
        Position merged = mergeAndFlush(position);
        return PositionResponse.fromEntity(merged);
    }

    @Override
    @Transactional
    public void deletePosition(UUID id) {
        Position position = positionRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Position not found with id: " + id));
        UUID officeId = position.getDepartment() != null && position.getDepartment().getOffice() != null
                ? position.getDepartment().getOffice().getId()
                : null;
        accessScopeService.assertCanManageOffice(officeId);
        ensureNoUsersAssignedToPosition(id);
        positionRepository.delete(position);
    }

    @Override
    @Transactional
    public void deletePositionInDepartment(UUID departmentId, UUID positionId) {
        Position position = positionRepository.findById(positionId).orElseThrow(
                () -> new ResourceNotFoundException("Position not found with id: " + positionId));
        if (!position.belongsToDepartment(departmentId)) {
            throw new BadRequestException("Position does not belong to the specified department");
        }
        UUID officeId = position.getDepartment() != null && position.getDepartment().getOffice() != null
                ? position.getDepartment().getOffice().getId()
                : null;
        accessScopeService.assertCanManageOffice(officeId);
        ensureNoUsersAssignedToPosition(positionId);
        positionRepository.delete(position);
    }

    private void ensureNoUsersAssignedToPosition(UUID positionId) {
        long assigned = userRepository.countByPosition_Id(positionId);
        if (assigned > 0) {
            throw new ConflictException(
                    "Cannot delete position while users are assigned to it. Reassign users first.");
        }
    }

//    private void assertExpectedVersion(Long expectedVersion, Long currentVersion, String resourceName) {
//        if (expectedVersion == null) {
//            throw new BadRequestException("Expected version is required");
//        }
//        if (!Objects.equals(expectedVersion, currentVersion)) {
//            throw new ConflictException(resourceName + " was modified by another user. Please refresh and retry.");
//        }
//    }

    private Position mergeAndFlush(Position position) {
        if (entityManager != null) {
            Position merged = entityManager.merge(position);
            entityManager.flush();
            return merged;
        }
        return positionRepository.saveAndFlush(position);
    }
}

