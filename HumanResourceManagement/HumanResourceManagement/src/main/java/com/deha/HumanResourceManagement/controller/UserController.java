package com.deha.HumanResourceManagement.controller;

import com.deha.HumanResourceManagement.dto.ApiResponse;
import com.deha.HumanResourceManagement.dto.PageResponse;
import com.deha.HumanResourceManagement.dto.user.AdminResetUserPasswordRequest;
import com.deha.HumanResourceManagement.dto.user.UserRequest;
import com.deha.HumanResourceManagement.dto.user.UpdateUserRequest;
import com.deha.HumanResourceManagement.dto.user.UserResponse;
import com.deha.HumanResourceManagement.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Tag(name = "Users", description = "User management APIs including create, update, deactivate, password reset and avatar operations")
@RestController
@RequestMapping("/api/users")
public class UserController extends ApiControllerSupport {
    private final IUserService userService;

    public UserController(IUserService userService) {
        this.userService = userService;
    }

    @PostMapping()
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER_OFFICE')")
    @Operation(summary = "Create user", description = "Create new user account and trigger activation email flow")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "User created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Email already exists")
    })
    public ApiResponse createUser(@RequestBody @Valid UserRequest userRequest) {
        return success("User created successfully", HttpStatus.CREATED, userService.createUser(userRequest));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER_OFFICE')")
    @Operation(summary = "Update user", description = "Update user profile, role, and organization assignment")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Version conflict")
    })
    public ApiResponse updateUser(@PathVariable UUID id, @RequestBody @Valid UpdateUserRequest UserRequest) {
        return success("User updated successfully", HttpStatus.OK, userService.updateUser(id, UserRequest));
    }

    @GetMapping()
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER_OFFICE','MANAGER_DEPARTMENT')")
    @Operation(summary = "List users", description = "Get users with keyword/filter/pagination based on caller scope")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Users retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
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
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER_OFFICE','MANAGER_DEPARTMENT')")
    @Operation(summary = "Get user by id", description = "Get user detail by id with access-scope enforcement")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ApiResponse getUserById(@PathVariable UUID id) {
        return success("User retrieved successfully", HttpStatus.OK, userService.getUserById(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER_OFFICE')")
    @Operation(summary = "Delete user", description = "Delete user when no dependent attendance/overtime/payroll records exist")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "User has related records and cannot be deleted")
    })
    public ApiResponse deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return success("User deleted successfully", HttpStatus.OK, null);
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER_OFFICE')")
    @Operation(summary = "Deactivate user", description = "Set user as inactive without deleting historical records")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "User deactivated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "User not found")
    })
    public ApiResponse deactivateUser(@PathVariable UUID id) {
        userService.deactivateUser(id);
        return success("User deactivated successfully", HttpStatus.OK, null);
    }

    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER_OFFICE')")
    @Operation(summary = "Reset user password", description = "Admin/manager reset password for target user in allowed scope")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Password reset"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Validation failed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ApiResponse resetUserPassword(@PathVariable UUID id, @RequestBody @Valid AdminResetUserPasswordRequest request) {
        userService.resetUserPassword(id, request.getNewPassword());
        return success("User password reset successfully", HttpStatus.OK, null);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER_OFFICE','MANAGER_DEPARTMENT')")
    @Operation(summary = "Search users", description = "Search users by keyword with optional organization/status filters")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Users retrieved"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Forbidden")
    })
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

    @PostMapping("/me/avatar")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Upload my avatar", description = "Upload and replace current user avatar image")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Avatar uploaded"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid file"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ApiResponse uploadAvatar(@RequestParam("file") MultipartFile file) {
        String avatarUrl = userService.uploadAvatar(file);
        return success("Avatar uploaded successfully", HttpStatus.OK, avatarUrl);
    }

    @DeleteMapping("/me/avatar")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Remove my avatar", description = "Delete current user avatar")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Avatar removed"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ApiResponse removeAvatar() {
        userService.removeAvatar();
        return success("Avatar removed successfully", HttpStatus.OK, null);
    }

}