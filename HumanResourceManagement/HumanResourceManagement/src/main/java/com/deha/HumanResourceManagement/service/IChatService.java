package com.deha.HumanResourceManagement.service;

import com.deha.HumanResourceManagement.dto.chat.ChatMessageRequest;
import com.deha.HumanResourceManagement.dto.chat.ChatMessageResponse;
import com.deha.HumanResourceManagement.dto.chat.ChatRoomResponse;
import com.deha.HumanResourceManagement.dto.chat.TypingEvent;
import com.deha.HumanResourceManagement.entity.ChatRoom;
import com.deha.HumanResourceManagement.entity.Department;
import com.deha.HumanResourceManagement.entity.Office;

import java.util.List;
import java.util.UUID;

public interface IChatService {
    ChatMessageResponse sendMessage(ChatMessageRequest request, String senderEmail);
    void broadcastTyping(TypingEvent event, String senderEmail);
    List<ChatMessageResponse> getHistory(UUID roomId, int page, int size);
    List<ChatRoomResponse> getMyRooms();
    ChatRoom createOfficeGeneralRoom(Office office);
    ChatRoom createDepartmentRoom(Department department);

}
