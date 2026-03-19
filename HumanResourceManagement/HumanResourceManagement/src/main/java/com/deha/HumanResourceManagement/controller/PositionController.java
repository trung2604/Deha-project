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
public class PositionController {
    private final PositionService positionService;

    public PositionController(PositionService positionService) {
        this.positionService = positionService;
    }

    @GetMapping()
    public ApiResponse getAllPositions() {
        try {
            ApiResponse response = new ApiResponse();
            response.setMessage("Positions retrieved successfully");
            response.setStatus(HttpStatus.OK.value());
            response.setData(positionService.getAllPositions());
            return response;
        } catch (Exception e) {
            ApiResponse response = new ApiResponse();
            response.setMessage("An error occurred while retrieving positions: " + e.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return response;
        }
    }

    @GetMapping("/{id}")
    public ApiResponse getPositionById(@PathVariable UUID id) {
        try {
            ApiResponse response = new ApiResponse();
            response.setMessage("Position retrieved successfully");
            response.setStatus(HttpStatus.OK.value());
            response.setData(positionService.getPositionById(id));
            return response;
        } catch (Exception e) {
            ApiResponse response = new ApiResponse();
            response.setMessage("An error occurred while retrieving position: " + e.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return response;
        }
    }

    @PutMapping("/{id}")
    public ApiResponse updatePosition(@PathVariable UUID id, @RequestParam UUID departmentId, @RequestBody @Valid PositionRequest positionRequest) {
        try {
            ApiResponse response = new ApiResponse();
            response.setMessage("Position updated successfully");
            response.setStatus(HttpStatus.OK.value());
            response.setData(positionService.updatePosition(id, departmentId, positionRequest));
            return response;
        } catch (Exception e) {
            ApiResponse response = new ApiResponse();
            response.setMessage("An error occurred while updating position: " + e.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return response;
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse deletePosition(@PathVariable UUID id) {
        try{
            positionService.deletePosition(id);
            ApiResponse response = new ApiResponse();
            response.setMessage("Position deleted successfully");
            response.setStatus(HttpStatus.OK.value());
            return response;
        } catch (Exception e) {
            ApiResponse response = new ApiResponse();
            response.setMessage("An error occurred while deleting position: " + e.getMessage());
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            return response;
        }
    }
}
