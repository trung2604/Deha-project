package com.deha.HumanResourceManagement.controller;

import com.deha.HumanResourceManagement.dto.ApiResponse;
import com.deha.HumanResourceManagement.dto.position.PositionRequest;
import com.deha.HumanResourceManagement.repository.PositionRepository;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/positions")
public class PositionController {
    private final PositionRepository positionRepository;

    public PositionController(PositionRepository positionRepository) {
        this.positionRepository = positionRepository;
    }

    @GetMapping()
    public ApiResponse getAllPositions() {
        try {
            ApiResponse response = new ApiResponse();
            response.setMessage("Positions retrieved successfully");
            response.setStatus(200);
            response.setData(positionRepository.findAll());
            return response;
        } catch (Exception e) {
            ApiResponse response = new ApiResponse();
            response.setMessage("An error occurred while retrieving positions: " + e.getMessage());
            response.setStatus(500);
            return response;
        }
    }

    @GetMapping("/{id}")
    public ApiResponse getPositionById() {
        try {
            ApiResponse response = new ApiResponse();
            response.setMessage("Position retrieved successfully");
            response.setStatus(200);
            response.setData(positionRepository.findAll());
            return response;
        } catch (Exception e) {
            ApiResponse response = new ApiResponse();
            response.setMessage("An error occurred while retrieving position: " + e.getMessage());
            response.setStatus(500);
            return response;
        }
    }

    @PostMapping()
    public ApiResponse addPosition(@RequestBody @Valid PositionRequest positionRequest) {
        try {
            ApiResponse response = new ApiResponse();
            response.setMessage("Position created successfully");
            response.setStatus(201);
            return response;
        } catch (Exception e) {
            ApiResponse response = new ApiResponse();
            response.setMessage("An error occurred while creating position: " + e.getMessage());
            response.setStatus(500);
            return response;
        }
    }

    @PutMapping("/{id}")
    public ApiResponse updatePosition(@PathVariable UUID id, @RequestBody @Valid PositionRequest positionRequest) {
        try {
            ApiResponse response = new ApiResponse();
            response.setMessage("Position updated successfully");
            response.setStatus(200);
            return response;
        } catch (Exception e) {
            ApiResponse response = new ApiResponse();
            response.setMessage("An error occurred while updating position: " + e.getMessage());
            response.setStatus(500);
            return response;
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse deletePosition(@PathVariable UUID id) {
        try{
            ApiResponse response = new ApiResponse();
            response.setMessage("Position deleted successfully");
            response.setStatus(200);
            return response;
        } catch (Exception e) {
            ApiResponse response = new ApiResponse();
            response.setMessage("An error occurred while deleting position: " + e.getMessage());
            response.setStatus(500);
            return response;
        }
    }
}
