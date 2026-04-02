package com.deha.HumanResourceManagement.controller;

import com.deha.HumanResourceManagement.dto.ApiResponse;
import com.deha.HumanResourceManagement.dto.department.DepartmentRequest;
import com.deha.HumanResourceManagement.dto.position.PositionRequest;
import com.deha.HumanResourceManagement.service.IDepartmentService;
import com.deha.HumanResourceManagement.service.IPositionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController extends ApiControllerSupport {
    private final IDepartmentService departmentService;
    private final IPositionService positionService;

    public DepartmentController(IDepartmentService departmentService, IPositionService positionService) {
        this.departmentService = departmentService;
        this.positionService = positionService;
    }

    @GetMapping()
    public ApiResponse getDepartments(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UUID officeId
    ) {
        return success("Departments retrieved successfully", HttpStatus.OK, departmentService.getDepartmentDirectory(keyword, officeId));
    }

    @GetMapping("/{id}")
    public ApiResponse getDepartmentById(@PathVariable UUID id) {
        return success("Department retrieved successfully", HttpStatus.OK, departmentService.getDepartmentDetailById(id));
    }

    @PostMapping()
    public ApiResponse createDepartment(@RequestBody @Valid DepartmentRequest departmentRequest) {
        return success("Department created successfully", HttpStatus.CREATED, departmentService.createDepartment(departmentRequest));
    }

    @PutMapping("/{id}")
    public ApiResponse updateDepartment(@PathVariable UUID id, @RequestBody @Valid DepartmentRequest departmentRequest) {
        return success("Department updated successfully", HttpStatus.OK, departmentService.updateDepartment(id, departmentRequest));
    }

    @DeleteMapping("/{id}")
    public ApiResponse deleteDepartment(@PathVariable UUID id) {
        departmentService.deleteDepartment(id);
        return success("Department deleted successfully", HttpStatus.OK, null);
    }

    @GetMapping("/{departmentId}/positions")
    public ApiResponse getDepartmentPositions(@PathVariable UUID departmentId) {
        return success("Positions retrieved successfully", HttpStatus.OK, positionService.getAllPositionsOfDepartment(departmentId));
    }

    @PostMapping("/{departmentId}/positions")
    public ApiResponse createDepartmentPosition(@PathVariable UUID departmentId, @RequestBody @Valid PositionRequest positionRequest) {
        return success("Position created successfully", HttpStatus.CREATED, positionService.createPosition(departmentId, positionRequest));
    }

    @PutMapping("/{departmentId}/positions/{positionId}")
    public ApiResponse updateDepartmentPosition(
            @PathVariable UUID departmentId,
            @PathVariable UUID positionId,
            @RequestBody @Valid PositionRequest positionRequest
    ) {
        return success("Position updated successfully", HttpStatus.OK, positionService.updatePosition(positionId, departmentId, positionRequest));
    }

    @DeleteMapping("/{departmentId}/positions/{positionId}")
    public ApiResponse deleteDepartmentPosition(@PathVariable UUID departmentId, @PathVariable UUID positionId) {
        positionService.deletePositionInDepartment(departmentId, positionId);
        return success("Position deleted successfully", HttpStatus.OK, null);
    }

}
