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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController extends ApiControllerSupport {
    private final IUserService userService;

    public UserController(IUserService userService) {
        this.userService = userService;
    }

    @PostMapping()
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ApiResponse createUser(@RequestBody @Valid UserRequest userRequest) {
        return success("User created successfully", HttpStatus.CREATED, userService.createUser(userRequest));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ApiResponse updateUser(@PathVariable UUID id, @RequestBody @Valid UpdateUserRequest UserRequest) {
        return success("User updated successfully", HttpStatus.OK, userService.updateUser(id, UserRequest));
    }

    @GetMapping()
    @PreAuthorize("hasAuthority('USER_VIEW')")
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
    @PreAuthorize("hasAuthority('USER_VIEW')")
    public ApiResponse getUserById(@PathVariable UUID id) {
        return success("User retrieved successfully", HttpStatus.OK, userService.getUserById(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ApiResponse deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return success("User deleted successfully", HttpStatus.OK, null);
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('USER_VIEW')")
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
    public ApiResponse uploadAvatar(@RequestParam("file") MultipartFile file) {
        String avatarUrl = userService.uploadAvatar(file);
        return success("Avatar uploaded successfully", HttpStatus.OK, avatarUrl);
    }

    @DeleteMapping("/me/avatar")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse removeAvatar() {
        userService.removeAvatar();
        return success("Avatar removed successfully", HttpStatus.OK, null);
    }

}