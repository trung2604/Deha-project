package com.deha.HumanResourceManagement.service;

import com.deha.HumanResourceManagement.dto.position.PositionRequest;
import com.deha.HumanResourceManagement.dto.position.PositionResponse;

import java.util.List;
import java.util.UUID;

public interface IPositionService {
    List<PositionResponse> getAllPositionsOfDepartment(UUID departmentId);

    List<PositionResponse> getAllPositions();

    PositionResponse getPositionById(UUID id);

    PositionResponse createPosition(UUID departmentId, PositionRequest request);

    PositionResponse updatePosition(UUID id, UUID departmentId, PositionRequest request);

    void deletePosition(UUID id);

    void deletePositionInDepartment(UUID departmentId, UUID positionId);
}

