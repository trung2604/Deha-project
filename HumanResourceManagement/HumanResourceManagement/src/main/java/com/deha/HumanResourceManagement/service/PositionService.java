package com.deha.HumanResourceManagement.service;

import com.deha.HumanResourceManagement.dto.position.PositionRequest;
import com.deha.HumanResourceManagement.dto.position.PositionResponse;
import com.deha.HumanResourceManagement.entity.Department;
import com.deha.HumanResourceManagement.entity.Position;
import com.deha.HumanResourceManagement.exception.ResourceAlreadyExistException;
import com.deha.HumanResourceManagement.exception.ResourceNotFoundException;
import com.deha.HumanResourceManagement.repository.PositionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PositionService {
    private final PositionRepository positionRepository;
    private final DepartmentService departmentService;

    public PositionService(PositionRepository positionRepository, DepartmentService departmentService) {
        this.departmentService = departmentService;
        this.positionRepository = positionRepository;
    }

    public List<PositionResponse> getAllPositionsOfDepartment(UUID departmentId) {
        return positionRepository.findAllByDepartmentId(departmentId).stream()
                .map(position -> new PositionResponse(
                        position.getId(),
                        position.getName(),
                        position.getDepartment() != null ? position.getDepartment().getId() : null,
                        position.getDepartment() != null ? position.getDepartment().getName() : null))
                .toList();
    }

    public List<PositionResponse> getAllPositions() {
        return positionRepository.findAll().stream()
                .map(position -> new PositionResponse(
                        position.getId(),
                        position.getName(),
                        position.getDepartment() != null ? position.getDepartment().getId() : null,
                        position.getDepartment() != null ? position.getDepartment().getName() : null))
                .toList();
    }

    public PositionResponse getPositionById(UUID id) {
        Position position = positionRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Position not found with id: " + id));
        return new PositionResponse(
                position.getId(),
                position.getName(),
                position.getDepartment() != null ? position.getDepartment().getId() : null,
                position.getDepartment() != null ? position.getDepartment().getName() : null
        );
    }

    @Transactional
    public PositionResponse createPosition(UUID departmentId, PositionRequest request) {
        Department department = departmentService.findDepartmentById(departmentId);

        if (positionRepository.existsByNameInDepartment(departmentId, request.getName())) {
            throw new ResourceAlreadyExistException("Position with the same name already exists in the department.");
        }

        Position newPosition = new Position();
        newPosition.setName(request.getName());
        newPosition.setDepartment(department);
        positionRepository.save(newPosition);

        return new PositionResponse(newPosition.getId(), newPosition.getName(), department.getId(), department.getName());
    }

    @Transactional
    public PositionResponse updatePosition(UUID id, UUID departmentId, PositionRequest request) {
        Position existingPosition = positionRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Position not found with id: " + id));

        Department department = departmentService.findDepartmentById(departmentId);
        if (!existingPosition.getDepartment().getId().equals(departmentId)) {
            existingPosition.setDepartment(department);
        }

        boolean nameChanged = request.getName() != null && !request.getName().equalsIgnoreCase(existingPosition.getName());
        if (nameChanged && positionRepository.existsByNameInDepartment(departmentId, request.getName())) {
            throw new ResourceAlreadyExistException("Position with the same name already exists in the department.");
        }

        existingPosition.setName(request.getName());
        positionRepository.save(existingPosition);
        return new PositionResponse(
                existingPosition.getId(),
                existingPosition.getName(),
                existingPosition.getDepartment() != null ? existingPosition.getDepartment().getId() : null,
                existingPosition.getDepartment() != null ? existingPosition.getDepartment().getName() : null
        );
    }

    @Transactional
    public void deletePosition(UUID id) {
        Position position = positionRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Position not found with id: " + id));
        positionRepository.delete(position);
    }

    @Transactional
    public void deletePositionInDepartment(UUID departmentId, UUID positionId) {
        Position position = positionRepository.findById(positionId).orElseThrow(
                () -> new ResourceNotFoundException("Position not found with id: " + positionId));
        if (position.getDepartment() == null || !position.getDepartment().getId().equals(departmentId)) {
            throw new IllegalArgumentException("Position does not belong to the specified department");
        }
        positionRepository.delete(position);
    }
}
