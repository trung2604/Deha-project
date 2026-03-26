package com.deha.HumanResourceManagement.controller;

import com.deha.HumanResourceManagement.dto.ApiResponse;
import com.deha.HumanResourceManagement.dto.position.PositionRequest;
import com.deha.HumanResourceManagement.service.PositionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/positions")
public class PositionController extends ApiControllerSupport {
    private final PositionService positionService;

    public PositionController(PositionService positionService) {
        this.positionService = positionService;
    }

    @GetMapping()
    public ApiResponse getAllPositions() {
        return success("Positions retrieved successfully", HttpStatus.OK, positionService.getAllPositions());
    }

    @GetMapping("/{id}")
    public ApiResponse getPositionById(@PathVariable UUID id) {
        return success("Position retrieved successfully", HttpStatus.OK, positionService.getPositionById(id));
    }

    @PutMapping("/{id}")
    public ApiResponse updatePosition(@PathVariable UUID id, @RequestParam UUID departmentId, @RequestBody @Valid PositionRequest positionRequest) {
        return success("Position updated successfully", HttpStatus.OK, positionService.updatePosition(id, departmentId, positionRequest));
    }

    @DeleteMapping("/{id}")
    public ApiResponse deletePosition(@PathVariable UUID id) {
        positionService.deletePosition(id);
        return success("Position deleted successfully", HttpStatus.OK, null);
    }

}
