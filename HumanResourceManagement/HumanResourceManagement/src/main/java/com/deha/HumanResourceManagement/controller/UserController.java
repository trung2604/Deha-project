package com.deha.HumanResourceManagement.controller;

import com.deha.HumanResourceManagement.dto.ApiResponse;
import com.deha.HumanResourceManagement.dto.user.UserRequest;
import com.deha.HumanResourceManagement.dto.user.UpdateUserRequest;
import com.deha.HumanResourceManagement.exception.ResourceAlreadyExistException;
import com.deha.HumanResourceManagement.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping()
    public ApiResponse createUser(@RequestBody @Valid UserRequest userRequest) {
        try {
            ApiResponse response = new ApiResponse();
            response.setMessage("User created successfully");
            response.setStatus(HttpStatus.CREATED.value());
            response.setData(userService.createUser(userRequest));
            return response;
        } catch (ResourceAlreadyExistException e) {
            ApiResponse response = new ApiResponse();
            response.setMessage(e.getMessage());
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return response;
        }
    }

    @PutMapping("/{id}")
    public ApiResponse updateUser(@PathVariable UUID id, @RequestBody @Valid UpdateUserRequest UserRequest) {
        try {
            ApiResponse response = new ApiResponse();
            response.setMessage("User updated successfully");
            response.setStatus(HttpStatus.OK.value());
            response.setData(userService.updateUser(id, UserRequest));
            return response;
        } catch (IllegalArgumentException e) {
            ApiResponse response = new ApiResponse();
            response.setMessage(e.getMessage());
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return response;
        }
    }

    @GetMapping()
    public ApiResponse getAllUsers() {
        ApiResponse response = new ApiResponse();
        response.setMessage("Users retrieved successfully");
        response.setStatus(HttpStatus.OK.value());
        response.setData(userService.getAllUsers());
        return response;
    }

    @GetMapping("/{id}")
    public ApiResponse getUserById(@PathVariable UUID id) {
        try {
            ApiResponse response = new ApiResponse();
            response.setMessage("User retrieved successfully");
            response.setStatus(HttpStatus.OK.value());
            response.setData(userService.getUserById(id));
            return response;
        } catch (IllegalArgumentException e) {
            ApiResponse response = new ApiResponse();
            response.setMessage(e.getMessage());
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return response;
        }
    }

    @DeleteMapping("/{id}")
    public ApiResponse deleteUser(@PathVariable UUID id) {
        try{
            userService.deleteUser(id);
            ApiResponse response = new ApiResponse();
            response.setMessage("User deleted successfully");
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