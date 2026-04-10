package com.deha.HumanResourceManagement.service;

import com.deha.HumanResourceManagement.dto.notification.NotificationResponse;
import com.deha.HumanResourceManagement.entity.ChatMessage;
import com.deha.HumanResourceManagement.entity.ChatRoom;
import com.deha.HumanResourceManagement.entity.User;

import java.util.List;
import java.util.UUID;

public interface INotificationService {
    void notifyNewMessage(ChatRoom room, ChatMessage message, User sender);
    void pushSystemNotification(UUID userId, String title, String body, String referenceId);
    List<NotificationResponse> getMyNotifications(User user, int page, int size);
    void markAsRead(UUID notificationId, User requester);
    void markAllAsRead(User user);
}
