package com.deha.HumanResourceManagement.controller;

import com.deha.HumanResourceManagement.dto.chat.ChatMessageRequest;
import com.deha.HumanResourceManagement.dto.chat.TypingEvent;
import com.deha.HumanResourceManagement.dto.ApiResponse;
import com.deha.HumanResourceManagement.exception.UnauthorizedException;
import com.deha.HumanResourceManagement.service.IChatService;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.UUID;


@RestController
@RequestMapping("/api/chat")
public class ChatController extends ApiControllerSupport {

    private final IChatService chatService;

    public ChatController(IChatService chatService) {
        this.chatService = chatService;
    }

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageRequest request, Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new UnauthorizedException("Missing websocket authentication");
        }
        chatService.sendMessage(request, principal.getName());
    }

    @MessageMapping("/chat.typing")
    public void typing(@Payload TypingEvent event, Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new UnauthorizedException("Missing websocket authentication");
        }
        chatService.broadcastTyping(event, principal.getName());
    }

    @GetMapping("/rooms")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse getMyRooms() {
        return success("Chat rooms retrieved successfully", HttpStatus.OK, chatService.getMyRooms());
    }

    @GetMapping("/history")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse getHistory(
            @RequestParam UUID roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size
    ) {
        return success("Chat history retrieved successfully", HttpStatus.OK, chatService.getHistory(roomId, page, size));
    }
}