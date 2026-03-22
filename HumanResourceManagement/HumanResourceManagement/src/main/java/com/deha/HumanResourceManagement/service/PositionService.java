package com.deha.HumanResourceManagement.service;

import com.deha.HumanResourceManagement.dto.position.PositionRequest;
import com.deha.HumanResourceManagement.dto.position.PositionResponse;
import com.deha.HumanResourceManagement.entity.Department;
import com.deha.HumanResourceManagement.entity.Position;
import com.deha.HumanResourceManagement.exception.BadRequestException;
import com.deha.HumanResourceManagement.exception.ConflictException;
import com.deha.HumanResourceManagement.exception.ResourceAlreadyExistException;
import com.deha.HumanResourceManagement.exception.ResourceNotFoundException;
import com.deha.HumanResourceManagement.repository.PositionRepository;
import com.deha.HumanResourceManagement.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PositionService {
    private final PositionRepository positionRepository;
    private final DepartmentService departmentService;
    private final UserRepository userRepository;

    public PositionService(
            PositionRepository positionRepository,
            DepartmentService departmentService,
            UserRepository userRepository
    ) {
        this.departmentService = departmentService;
        this.positionRepository = positionRepository;
        this.userRepository = userRepository;
    }

    public List<PositionResponse> getAllPositionsOfDepartment(UUID departmentId) {
        return positionRepository.findAllByDepartmentId(departmentId).stream()
                .map(PositionResponse::fromEntity)
                .toList();
    }

    public List<PositionResponse> getAllPositions() {
        return positionRepository.findAll().stream()
                .map(PositionResponse::fromEntity)
                .toList();
    }

    public PositionResponse getPositionById(UUID id) {
        Position position = positionRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Position not found with id: " + id));
        return PositionResponse.fromEntity(position);
    }

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

    @Transactional
    public PositionResponse updatePosition(UUID id, UUID departmentId, PositionRequest request) {
        Position existingPosition = positionRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Position not found with id: " + id));

        Department department = departmentService.findDepartmentById(departmentId);
        if (!existingPosition.belongsToDepartment(departmentId)) {
            existingPosition.assignDepartment(department);
        }

        boolean nameChanged = existingPosition.isNameChanged(request.getName());
        if (nameChanged && positionRepository.existsByNameInDepartment(departmentId, request.getName())) {
            throw new ResourceAlreadyExistException("Position with the same name already exists in the department.");
        }

        existingPosition.rename(request.getName());
        positionRepository.save(existingPosition);
        return PositionResponse.fromEntity(existingPosition);
    }

    @Transactional
    public void deletePosition(UUID id) {
        Position position = positionRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Position not found with id: " + id));
        ensureNoUsersAssignedToPosition(id);
        positionRepository.delete(position);
    }

    @Transactional
    public void deletePositionInDepartment(UUID departmentId, UUID positionId) {
        Position position = positionRepository.findById(positionId).orElseThrow(
                () -> new ResourceNotFoundException("Position not found with id: " + positionId));
        if (!position.belongsToDepartment(departmentId)) {
            throw new BadRequestException("Position does not belong to the specified department");
        }
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
}
