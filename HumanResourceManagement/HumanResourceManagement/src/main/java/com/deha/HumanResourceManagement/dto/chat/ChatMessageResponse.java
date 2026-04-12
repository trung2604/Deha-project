package com.deha.HumanResourceManagement.dto.chat;

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
}