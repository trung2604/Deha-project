package com.deha.HumanResourceManagement.dto.chat;

import lombok.Data;

import java.util.UUID;

@Data
public class TypingEvent {

    private UUID roomId;
    private UUID userId;
    private String userName;
    private boolean typing;

}
