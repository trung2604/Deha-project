package com.deha.HumanResourceManagement.controller;

import com.deha.HumanResourceManagement.dto.ApiResponse;
import com.deha.HumanResourceManagement.dto.PageResponse;
import com.deha.HumanResourceManagement.dto.user.UserRequest;
import com.deha.HumanResourceManagement.dto.user.UpdateUserRequest;
import com.deha.HumanResourceManagement.dto.user.UserResponse;
import com.deha.HumanResourceManagement.service.IUserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController extends ApiControllerSupport {
    private final IUserService userService;

    public UserController(IUserService userService) {
        this.userService = userService;
    }

    @PostMapping()
    public ApiResponse createUser(@RequestBody @Valid UserRequest userRequest) {
        return success("User created successfully", HttpStatus.CREATED, userService.createUser(userRequest));
    }

    @PutMapping("/{id}")
    public ApiResponse updateUser(@PathVariable UUID id, @RequestBody @Valid UpdateUserRequest UserRequest) {
        return success("User updated successfully", HttpStatus.OK, userService.updateUser(id, UserRequest));
    }

    @GetMapping()
    public ApiResponse getAllUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UUID officeId,
            @RequestParam(required = false) UUID departmentId,
            @RequestParam(required = false) UUID positionId,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(page = 0, size = 10) Pageable pageable
    ) {
        Page<UserResponse> results = userService.getUsersWithFilters(keyword, officeId, departmentId, positionId, active, pageable);
        return success("Users retrieved successfully", HttpStatus.OK, PageResponse.fromPage(results));
    }

    @GetMapping("/{id}")
    public ApiResponse getUserById(@PathVariable UUID id) {
        return success("User retrieved successfully", HttpStatus.OK, userService.getUserById(id));
    }

    @DeleteMapping("/{id}")
    public ApiResponse deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return success("User deleted successfully", HttpStatus.OK, null);
    }

    @GetMapping("/search")
    public ApiResponse searchUsers(
            @RequestParam String keyword,
            @RequestParam(required = false) UUID officeId,
            @RequestParam(required = false) UUID departmentId,
            @RequestParam(required = false) UUID positionId,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(page = 0, size = 10, sort = "firstName") Pageable pageable) {
        Page<UserResponse> results = userService.getUsersWithFilters(keyword, officeId, departmentId, positionId, active, pageable);
        return success("Users retrieved successfully", HttpStatus.OK, PageResponse.fromPage(results));
    }

}