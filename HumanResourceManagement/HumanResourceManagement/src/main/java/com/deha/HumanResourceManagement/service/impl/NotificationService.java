package com.deha.HumanResourceManagement.service.impl;

import com.deha.HumanResourceManagement.dto.notification.NotificationResponse;
import com.deha.HumanResourceManagement.entity.ChatMessage;
import com.deha.HumanResourceManagement.entity.ChatRoom;
import com.deha.HumanResourceManagement.entity.Notification;
import com.deha.HumanResourceManagement.entity.User;
import com.deha.HumanResourceManagement.entity.enums.NotificationType;
import com.deha.HumanResourceManagement.entity.enums.Role;
import com.deha.HumanResourceManagement.exception.ResourceNotFoundException;
import com.deha.HumanResourceManagement.repository.NotificationRepository;
import com.deha.HumanResourceManagement.repository.UserRepository;
import com.deha.HumanResourceManagement.service.INotificationService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class NotificationService implements INotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            SimpMessagingTemplate messagingTemplate
    ) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    @Async
    @Transactional
    public void notifyNewMessage(ChatRoom room, ChatMessage message, User sender) {
        List<User> recipients = resolveRecipients(room, sender);

        for (User recipient : recipients) {
            Notification notification = new Notification();
            notification.setUser(recipient);
            notification.setType(NotificationType.NEW_MESSAGE);
            notification.setTitle("Tin nhắn mới từ " + sender.getFirstName());
            notification.setBody(truncate(message.getContent(), 80));
            notification.setReferenceId(room.getId().toString());
            notificationRepository.save(notification);
            NotificationResponse payload = NotificationResponse.fromEntity(notification);
            messagingTemplate.convertAndSendToUser(
                    recipient.getEmail(),
                    "/queue/notify",
                    payload
            );
        }
    }

    @Override
    @Async
    @Transactional
    public void pushSystemNotification(UUID userId, String title, String body, String referenceId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(NotificationType.SYSTEM);
        notification.setTitle(title);
        notification.setBody(body);
        notification.setReferenceId(referenceId);
        notificationRepository.save(notification);

        messagingTemplate.convertAndSendToUser(
                user.getEmail(),
                "/queue/notify",
                NotificationResponse.fromEntity(notification)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications(User user, int page, int size) {
        return notificationRepository
                .findByUserOrderByCreatedAtDesc(user,
                        PageRequest.of(page, size, Sort.by("createdAt").descending()))
                .stream()
                .map(NotificationResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public void markAsRead(UUID notificationId, User requester) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found"));

        if (!notification.getUser().getId().equals(requester.getId())) {
            throw new com.deha.HumanResourceManagement.exception.ForbiddenException(
                    "Cannot mark another user's notification as read");
        }
        notification.setRead(true);
    }

    @Transactional
    public void markAllAsRead(User user) {
        notificationRepository.markAllReadByUser(user);
    }

    private List<User> resolveRecipients(ChatRoom room, User sender) {
        return switch (room.getType()) {
            case GENERAL ->
                    userRepository.findByOffice_Id(
                                    room.getOffice() != null ? room.getOffice().getId() : null
                            ).stream()
                            .filter(User::isActive)
                            .filter(u -> u.getRole() != Role.ADMIN)
                            .filter(u -> !u.getId().equals(sender.getId()))
                            .toList();
            case DEPARTMENT ->
                    userRepository.findByDepartment(room.getDepartment()).stream()
                            .filter(User::isActive)
                            .filter(u -> u.getRole() != Role.ADMIN)
                            .filter(u -> !u.getId().equals(sender.getId()))
                            .toList();
        };
    }

    private String truncate(String text, int maxLen) {
        if (text == null) return "";
        return text.length() <= maxLen ? text : text.substring(0, maxLen) + "...";
    }
}
