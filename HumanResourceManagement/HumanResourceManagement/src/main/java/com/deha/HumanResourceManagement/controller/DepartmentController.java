package com.deha.HumanResourceManagement.controller;

import com.deha.HumanResourceManagement.dto.ApiResponse;
import com.deha.HumanResourceManagement.dto.department.DepartmentRequest;
import com.deha.HumanResourceManagement.dto.position.PositionRequest;
import com.deha.HumanResourceManagement.service.DepartmentService;
import com.deha.HumanResourceManagement.service.PositionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {
    private final DepartmentService departmentService;
    private final PositionService positionService;

    public DepartmentController(DepartmentService departmentService, PositionService positionService) {
        this.departmentService = departmentService;
        this.positionService = positionService;
    }

    @GetMapping()
    public ApiResponse getAllDepartment() {
        ApiResponse response = new ApiResponse();
        response.setMessage("Department retrieved successfully");
        response.setStatus(HttpStatus.OK.value());
        response.setData(departmentService.getAllDepartments());
        return response;
    }

    @GetMapping("/{id}")
    public ApiResponse getDepartmentById(@PathVariable UUID id) {
        try{
            ApiResponse response = new ApiResponse();
            response.setMessage("Department retrieved successfully");
            response.setStatus(HttpStatus.OK.value());
            response.setData(departmentService.getDepartmentDetailById(id));
            return response;
        } catch (IllegalArgumentException e) {
            ApiResponse response = new ApiResponse();
            response.setMessage(e.getMessage());
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return response;
        }
    }

    @PostMapping()
    public ApiResponse createDepartment(@RequestBody @Valid DepartmentRequest departmentRequest) {
        try {
            ApiResponse response = new ApiResponse();
            response.setMessage("Department created successfully");
            response.setStatus(HttpStatus.CREATED.value());
            response.setData(departmentService.createDepartment(departmentRequest));
            return response;
        } catch (IllegalArgumentException e) {
            ApiResponse response = new ApiResponse();
            response.setMessage(e.getMessage());
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return response;
        }
    }

    @PutMapping("/{id}")
    public ApiResponse updateDepartment(@PathVariable UUID id, @RequestBody @Valid DepartmentRequest departmentRequest) {
        try {
            ApiResponse response = new ApiResponse();
            response.setMessage("Department updated successfully");
            response.setStatus(HttpStatus.OK.value());
            response.setData(departmentService.updateDepartment(id, departmentRequest));
            return response;
        } catch (IllegalArgumentException e) {
            ApiResponse response = new ApiResponse();
            response.setMessage(e.getMessage());
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return response;
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse deleteDepartment(@PathVariable UUID id) {
        try {
            departmentService.deleteDepartment(id);
            ApiResponse response = new ApiResponse();
            response.setMessage("Department deleted successfully");
            response.setStatus(HttpStatus.OK.value());
            return response;
        } catch (IllegalArgumentException e) {
            ApiResponse response = new ApiResponse();
            response.setMessage(e.getMessage());
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return response;
        }
    }

    @GetMapping("/{departmentId}/positions")
    public ApiResponse getDepartmentPositions(@PathVariable UUID departmentId) {
        ApiResponse response = new ApiResponse();
        response.setMessage("Positions retrieved successfully");
        response.setStatus(HttpStatus.OK.value());
        response.setData(positionService.getAllPositionsOfDepartment(departmentId));
        return response;
    }

    @PostMapping("/{departmentId}/positions")
    public ApiResponse createDepartmentPosition(@PathVariable UUID departmentId, @RequestBody @Valid PositionRequest positionRequest) {
        ApiResponse response = new ApiResponse();
        response.setMessage("Position created successfully");
        response.setStatus(HttpStatus.CREATED.value());
        response.setData(positionService.createPosition(departmentId, positionRequest));
        return response;
    }

    @PutMapping("/{departmentId}/positions/{positionId}")
    public ApiResponse updateDepartmentPosition(
            @PathVariable UUID departmentId,
            @PathVariable UUID positionId,
            @RequestBody @Valid PositionRequest positionRequest
    ) {
        ApiResponse response = new ApiResponse();
        response.setMessage("Position updated successfully");
        response.setStatus(HttpStatus.OK.value());
        response.setData(positionService.updatePosition(positionId, departmentId, positionRequest));
        return response;
    }

    @DeleteMapping("/{departmentId}/positions/{positionId}")
    public ApiResponse deleteDepartmentPosition(@PathVariable UUID departmentId, @PathVariable UUID positionId) {
        positionService.deletePositionInDepartment(departmentId, positionId);
        ApiResponse response = new ApiResponse();
        response.setMessage("Position deleted successfully");
        response.setStatus(HttpStatus.OK.value());
        return response;
    }
}
