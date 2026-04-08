package com.deha.HumanResourceManagement.controller;

import com.deha.HumanResourceManagement.dto.ApiResponse;
import com.deha.HumanResourceManagement.dto.position.PositionRequest;
import com.deha.HumanResourceManagement.service.IPositionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Tag(name = "Positions", description = "Position catalog management APIs")
@RestController
@RequestMapping("/api/positions")
public class PositionController extends ApiControllerSupport {
    private final IPositionService positionService;

    public PositionController(IPositionService positionService) {
        this.positionService = positionService;
    }

    @GetMapping()
    @PreAuthorize("hasAuthority('POSITION_VIEW')")
    public ApiResponse getAllPositions() {
        return success("Positions retrieved successfully", HttpStatus.OK, positionService.getAllPositions());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('POSITION_VIEW')")
    public ApiResponse getPositionById(@PathVariable UUID id) {
        return success("Position retrieved successfully", HttpStatus.OK, positionService.getPositionById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('POSITION_MANAGE')")
    public ApiResponse updatePosition(@PathVariable UUID id, @RequestParam UUID departmentId, @RequestBody @Valid PositionRequest positionRequest) {
        return success("Position updated successfully", HttpStatus.OK, positionService.updatePosition(id, departmentId, positionRequest));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('POSITION_MANAGE')")
    public ApiResponse deletePosition(@PathVariable UUID id) {
        positionService.deletePosition(id);
        return success("Position deleted successfully", HttpStatus.OK, null);
    }

}
