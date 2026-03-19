package com.deha.HumanResourceManagement.service;

import com.deha.HumanResourceManagement.dto.position.PositionRequest;
import com.deha.HumanResourceManagement.dto.position.PositionResponse;
import com.deha.HumanResourceManagement.entity.Position;
import com.deha.HumanResourceManagement.exception.ResourceNotFoundException;
import com.deha.HumanResourceManagement.repository.PositionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PositionService {
    private final PositionRepository positionRepository;

    public PositionService(PositionRepository positionRepository) {
        this.positionRepository = positionRepository;
    }

    public List<PositionResponse> getAllPositions() {
        return positionRepository.findAll().stream()
                .map(position -> new PositionResponse(
                        position.getId(),
                        position.getName(),
                        position.getDepartment()))
                .toList();
    }

    public PositionResponse getPositionById(UUID id) {
        Position position = positionRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Position not found with id: " + id));
        return new PositionResponse(
                position.getId(),
                position.getName(),
                position.getDepartment()
        );
    }

    public PositionResponse createPosition(PositionRequest position) {
        if(positionRepository.existsByName(position.getName())) {
            throw new IllegalArgumentException("Position with the same id already exists.");
        }
        Position newPosition = new Position();
        newPosition.setName(position.getName());
        newPosition.setDepartment(position.getDepartment());
        positionRepository.save(newPosition);
        return new PositionResponse(
                newPosition.getId(),
                newPosition.getName(),
                newPosition.getDepartment()
        );
    }

    public PositionResponse updatePosition(UUID id, PositionRequest position) {
        Position existingPosition = positionRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Position not found with id: " + id));
        existingPosition.setName(position.getName());
        existingPosition.setDepartment(position.getDepartment());
        positionRepository.save(existingPosition);
        return new PositionResponse(
                existingPosition.getId(),
                existingPosition.getName(),
                existingPosition.getDepartment()
        );
    }

    public void deletePosition(UUID id) {
        Position position = positionRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Position not found with id: " + id));
        positionRepository.delete(position);
    }
}
