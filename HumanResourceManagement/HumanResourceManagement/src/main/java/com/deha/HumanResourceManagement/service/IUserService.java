package com.deha.HumanResourceManagement.service;

import com.deha.HumanResourceManagement.dto.user.UpdateUserRequest;
import com.deha.HumanResourceManagement.dto.user.UserRequest;
import com.deha.HumanResourceManagement.dto.user.UserResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public interface IUserService {
    UserResponse createUser(UserRequest userRequest);

    UserResponse updateUser(UUID id, UpdateUserRequest userRequest);

    UserResponse getUserById(UUID id);

    void deleteUser(UUID id);

    void deactivateUser(UUID id);

    void resetUserPassword(UUID id, String newPassword);

    String uploadAvatar(MultipartFile file);

    void removeAvatar();

    Page<UserResponse> getUsersWithFilters(
            String keyword,
            UUID officeId,
            UUID departmentId,
            UUID positionId,
            Boolean active,
            Pageable pageable
    );
}

