package com.deha.HumanResourceManagement.controller;

import com.deha.HumanResourceManagement.dto.ApiResponse;
import com.deha.HumanResourceManagement.dto.employee.EmployeeRequest;
import com.deha.HumanResourceManagement.dto.employee.UpdateEmployeeRequest;
import com.deha.HumanResourceManagement.exception.ResourceAlreadyExistException;
import com.deha.HumanResourceManagement.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/employees")
public class EmployeeController {
    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping()
    public ApiResponse createEmployee(@RequestBody @Valid EmployeeRequest employeeRequest) {
        try {
            ApiResponse response = new ApiResponse();
            response.setMessage("Employee created successfully");
            response.setStatus(HttpStatus.CREATED.value());
            response.setData(employeeService.createEmployee(employeeRequest));
            return response;
        } catch (ResourceAlreadyExistException e) {
            ApiResponse response = new ApiResponse();
            response.setMessage(e.getMessage());
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return response;
        }
    }

    @PutMapping("/{id}")
    public ApiResponse updateEmployee(@PathVariable UUID id, @RequestBody @Valid UpdateEmployeeRequest employeeRequest) {
        try {
            ApiResponse response = new ApiResponse();
            response.setMessage("Employee updated successfully");
            response.setStatus(HttpStatus.OK.value());
            response.setData(employeeService.updateEmployee(id, employeeRequest));
            return response;
        } catch (IllegalArgumentException e) {
            ApiResponse response = new ApiResponse();
            response.setMessage(e.getMessage());
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return response;
        }
    }

    @GetMapping()
    public ApiResponse getAllEmployees() {
        ApiResponse response = new ApiResponse();
        response.setMessage("Employees retrieved successfully");
        response.setStatus(HttpStatus.OK.value());
        response.setData(employeeService.getAllEmployees());
        return response;
    }

    @GetMapping("/{id}")
    public ApiResponse getEmployeeById(@PathVariable UUID id) {
        try {
            ApiResponse response = new ApiResponse();
            response.setMessage("Employee retrieved successfully");
            response.setStatus(HttpStatus.OK.value());
            response.setData(employeeService.getEmployee(id));
            return response;
        } catch (IllegalArgumentException e) {
            ApiResponse response = new ApiResponse();
            response.setMessage(e.getMessage());
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return response;
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse deleteEmployee(@PathVariable UUID id) {
        try{
            employeeService.deleteEmployee(id);
            ApiResponse response = new ApiResponse();
            response.setMessage("Employee deleted successfully");
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