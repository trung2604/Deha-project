package com.deha.HumanResourceManagement.controller;

import com.deha.HumanResourceManagement.config.security.AccessScopeService;
import com.deha.HumanResourceManagement.dto.ApiResponse;
import com.deha.HumanResourceManagement.dto.notification.NotificationResponse;
import com.deha.HumanResourceManagement.entity.User;
import com.deha.HumanResourceManagement.exception.UnauthorizedException;
import com.deha.HumanResourceManagement.repository.UserRepository;
import com.deha.HumanResourceManagement.exception.ResourceNotFoundException;
import com.deha.HumanResourceManagement.service.INotificationService;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController extends ApiControllerSupport {

    private final INotificationService notificationService;
    private final AccessScopeService accessScopeService;
    private final UserRepository userRepository;

    public NotificationController(
            INotificationService notificationService,
            AccessScopeService accessScopeService,
            UserRepository userRepository
    ) {
        this.notificationService = notificationService;
        this.accessScopeService = accessScopeService;
        this.userRepository = userRepository;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ApiResponse getMyNotifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        User user = accessScopeService.currentUserOrThrow();
        List<NotificationResponse> notifications = notificationService.getMyNotifications(user, page, size);
        return success("Notifications retrieved successfully", HttpStatus.OK, notifications);
    }

    @PatchMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse markAsRead(@PathVariable UUID id) {
        User user = accessScopeService.currentUserOrThrow();
        notificationService.markAsRead(id, user);
        return success("Notification marked as read", HttpStatus.OK, null);
    }

    @PatchMapping("/read-all")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse markAllAsRead() {
        User user = accessScopeService.currentUserOrThrow();
        notificationService.markAllAsRead(user);
        return success("All notifications marked as read", HttpStatus.OK, null);
    }

    @MessageMapping("/notify.read")
    public void markReadViaWebSocket(UUID notificationId, java.security.Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new UnauthorizedException("Missing websocket authentication");
        }
        User user = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        notificationService.markAsRead(notificationId, user);
    }
}