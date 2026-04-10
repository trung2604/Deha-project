package com.deha.HumanResourceManagement.dto.chat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class ChatMessageRequest {

    @NotNull(message = "Room ID is required")
    private UUID roomId;

    @NotBlank(message = "Content is required")
    private String content;

}