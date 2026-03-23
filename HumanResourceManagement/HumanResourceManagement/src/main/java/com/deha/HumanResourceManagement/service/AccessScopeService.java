package com.deha.HumanResourceManagement.service;

import com.deha.HumanResourceManagement.entity.Role;
import com.deha.HumanResourceManagement.entity.User;
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

    public boolean isManager(User user) {
        return user != null && user.getRole() == Role.ROLE_MANAGER;
    }

    public void assertCanManageOffice(UUID targetOfficeId) {
        User actor = currentUserOrThrow();
        if (isAdmin(actor)) return;
        if (!isManager(actor)) {
            throw new ForbiddenException("Only admin or manager can manage office-scoped data");
        }
        UUID managerOfficeId = actor.getOffice() != null ? actor.getOffice().getId() : null;
        if (managerOfficeId == null || targetOfficeId == null || !managerOfficeId.equals(targetOfficeId)) {
            throw new ForbiddenException("Manager can only manage data in their own office");
        }
    }
}
