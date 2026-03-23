package com.deha.HumanResourceManagement.controller;

import com.deha.HumanResourceManagement.dto.ApiResponse;
import com.deha.HumanResourceManagement.dto.office.OfficeRequest;
import com.deha.HumanResourceManagement.service.OfficeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/offices")
public class OfficeController {
    private final OfficeService officeService;

    public OfficeController(OfficeService officeService) {
        this.officeService = officeService;
    }

    @GetMapping
    public ApiResponse getAll() {
        return success("Offices retrieved successfully", HttpStatus.OK, officeService.getAll());
    }

    @PostMapping
    public ApiResponse create(@RequestBody @Valid OfficeRequest request) {
        return success("Office created successfully", HttpStatus.CREATED, officeService.create(request));
    }

    @PutMapping("/{id}")
    public ApiResponse update(@PathVariable UUID id, @RequestBody @Valid OfficeRequest request) {
        return success("Office updated successfully", HttpStatus.OK, officeService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse delete(@PathVariable UUID id) {
        officeService.delete(id);
        return success("Office deleted successfully", HttpStatus.OK, null);
    }

    private ApiResponse success(String message, HttpStatus status, Object data) {
        ApiResponse response = new ApiResponse();
        response.setMessage(message);
        response.setStatus(status.value());
        response.setData(data);
        return response;
    }
}
