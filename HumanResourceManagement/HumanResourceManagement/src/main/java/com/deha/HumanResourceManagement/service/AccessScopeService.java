package com.deha.HumanResourceManagement.service;

import com.deha.HumanResourceManagement.entity.enums.Role;
import com.deha.HumanResourceManagement.entity.User;
import com.deha.HumanResourceManagement.exception.ResourceNotFoundException;
import com.deha.HumanResourceManagement.exception.ForbiddenException;
import com.deha.HumanResourceManagement.exception.UnauthorizedException;
import com.deha.HumanResourceManagement.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class AccessScopeService {
    private final UserRepository userRepository;

    public AccessScopeService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User currentUserOrThrow() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new UnauthorizedException("Missing authentication context");
        }
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new UnauthorizedException("Authenticated user not found"));
    }

    public boolean isAdmin(User user) {
        return user != null && user.getRole() == Role.ROLE_ADMIN;
    }

    public boolean isOfficeManager(User user) {
        return user != null && user.getRole() == Role.ROLE_MANAGER_OFFICE;
    }

    public boolean isDepartmentManager(User user) {
        return user != null && user.getRole() == Role.ROLE_MANAGER_DEPARTMENT;
    }

    public boolean isManager(User user) {
        return isOfficeManager(user) || isDepartmentManager(user);
    }

    public void assertCanManageOffice(UUID targetOfficeId) {
        User actor = currentUserOrThrow();
        if (isAdmin(actor)) return;
        if (!isOfficeManager(actor)) {
            throw new ForbiddenException("Only admin or manager can manage office-scoped data");
        }
        UUID managerOfficeId = actor.getOffice() != null ? actor.getOffice().getId() : null;
        if (managerOfficeId == null || targetOfficeId == null || !managerOfficeId.equals(targetOfficeId)) {
            throw new ForbiddenException("Manager can only manage data in their own office");
        }
    }

    public void assertCanManageDepartment(UUID targetDepartmentId) {
        User actor = currentUserOrThrow();
        if (isAdmin(actor)) return;
        if (!isDepartmentManager(actor)) {
            throw new ForbiddenException("Only admin or department manager can manage department-scoped data");
        }
        UUID actorDepartmentId = actor.getDepartment() != null ? actor.getDepartment().getId() : null;
        if (actorDepartmentId == null || targetDepartmentId == null || !actorDepartmentId.equals(targetDepartmentId)) {
            throw new ForbiddenException("Department manager can only manage data in their own department");
        }
    }

    public void assertCanAccessUser(User targetUser) {
        if (targetUser == null) {
            throw new ResourceNotFoundException("User not found");
        }
        User actor = currentUserOrThrow();
        if (isAdmin(actor)) return;

        if (isOfficeManager(actor)) {
            UUID actorOfficeId = actor.getOffice() != null ? actor.getOffice().getId() : null;
            UUID targetOfficeId = targetUser.getOffice() != null ? targetUser.getOffice().getId() : null;
            if (actorOfficeId == null || targetOfficeId == null || !actorOfficeId.equals(targetOfficeId)) {
                throw new ForbiddenException("Office manager can only access users in their own office");
            }
            return;
        }

        if (isDepartmentManager(actor)) {
            UUID actorDepartmentId = actor.getDepartment() != null ? actor.getDepartment().getId() : null;
            UUID targetDepartmentId = targetUser.getDepartment() != null ? targetUser.getDepartment().getId() : null;
            if (actorDepartmentId == null || targetDepartmentId == null || !actorDepartmentId.equals(targetDepartmentId)) {
                throw new ForbiddenException("Department manager can only access users in their own department");
            }
            return;
        }

        throw new ForbiddenException("You do not have permission to access this user");
    }

    public void assertCanAccessUserId(UUID targetUserId) {
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        assertCanAccessUser(targetUser);
    }
}
