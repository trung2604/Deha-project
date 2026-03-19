package com.deha.HumanResourceManagement.controller;

import com.deha.HumanResourceManagement.dto.ApiResponse;
import com.deha.HumanResourceManagement.dto.department.DepartmentRequest;
import com.deha.HumanResourceManagement.service.DepartmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {
    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
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
            response.setData(departmentService.getDepartmentById(id));
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
}
