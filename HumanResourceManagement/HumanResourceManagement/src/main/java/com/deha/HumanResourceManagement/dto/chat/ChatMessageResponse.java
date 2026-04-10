package com.deha.HumanResourceManagement.dto.chat;

import com.deha.HumanResourceManagement.entity.ChatMessage;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class ChatMessageResponse {

    private UUID id;
    private UUID roomId;
    private UUID senderId;
    private String senderName;
    private String content;
    private Instant sentAt;

    public static ChatMessageResponse fromEntity(ChatMessage msg) {
        ChatMessageResponse r = new ChatMessageResponse();
        r.id = msg.getId();
        r.roomId = msg.getRoom().getId();
        r.senderId = msg.getSender().getId();
        r.senderName = msg.getSender().getFirstName() + " " + msg.getSender().getLastName();
        r.content = msg.getContent();
        r.sentAt = msg.getSentAt();
        return r;
    }
}