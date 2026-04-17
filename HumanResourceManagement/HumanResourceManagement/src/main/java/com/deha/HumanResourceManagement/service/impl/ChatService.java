package com.deha.HumanResourceManagement.service.impl;

import com.deha.HumanResourceManagement.config.security.AccessScopeService;
import com.deha.HumanResourceManagement.dto.chat.ChatMessageRequest;
import com.deha.HumanResourceManagement.dto.chat.ChatMessageResponse;
import com.deha.HumanResourceManagement.dto.chat.ChatRoomResponse;
import com.deha.HumanResourceManagement.dto.chat.TypingEvent;
import com.deha.HumanResourceManagement.entity.ChatMessage;
import com.deha.HumanResourceManagement.entity.ChatRoom;
import com.deha.HumanResourceManagement.entity.Department;
import com.deha.HumanResourceManagement.entity.Office;
import com.deha.HumanResourceManagement.entity.User;
import com.deha.HumanResourceManagement.entity.enums.ChatRoomType;
import com.deha.HumanResourceManagement.exception.ForbiddenException;
import com.deha.HumanResourceManagement.exception.ResourceNotFoundException;
import com.deha.HumanResourceManagement.exception.UnauthorizedException;
import com.deha.HumanResourceManagement.mapper.chat.ChatMessageMapper;
import com.deha.HumanResourceManagement.mapper.chat.ChatRoomMapper;
import com.deha.HumanResourceManagement.repository.ChatMessageRepository;
import com.deha.HumanResourceManagement.repository.ChatRoomRepository;
import com.deha.HumanResourceManagement.repository.UserRepository;
import com.deha.HumanResourceManagement.service.IChatService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class ChatService implements IChatService {
    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final AccessScopeService accessScopeService;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageMapper chatMessageMapper;
    private final ChatRoomMapper chatRoomMapper;

    public ChatService(
            ChatRoomRepository chatRoomRepository,
            ChatMessageRepository chatMessageRepository,
            UserRepository userRepository,
            AccessScopeService accessScopeService,
            NotificationService notificationService,
            SimpMessagingTemplate messagingTemplate,
            ChatMessageMapper chatMessageMapper,
            ChatRoomMapper chatRoomMapper
    ) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.userRepository = userRepository;
        this.accessScopeService = accessScopeService;
        this.notificationService = notificationService;
        this.messagingTemplate = messagingTemplate;
        this.chatMessageMapper = chatMessageMapper;
        this.chatRoomMapper = chatRoomMapper;
    }

    @Override
    @Transactional
    public ChatMessageResponse sendMessage(ChatMessageRequest request, String senderEmail) {
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new UnauthorizedException("Authenticated user not found"));
        ChatRoom room = chatRoomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Chat room not found"));

        assertCanAccessRoom(sender, room);

        ChatMessage message = new ChatMessage();
        message.setRoom(room);
        message.setSender(sender);
        message.setContent(request.getContent().trim());
        ChatMessage persistedMessage = chatMessageRepository.saveAndFlush(message);
        ChatMessageResponse response = chatMessageMapper.toResponse(persistedMessage);
        String topic = resolveTopic(room);
        messagingTemplate.convertAndSend(topic, response);
        notificationService.notifyNewMessage(room, persistedMessage, sender);

        return response;
    }

    @Override
    public void broadcastTyping(TypingEvent event, String senderEmail) {
        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new UnauthorizedException("Authenticated user not found"));
        ChatRoom room = chatRoomRepository.findById(event.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Chat room not found"));

        assertCanAccessRoom(sender, room);

        event.setUserId(sender.getId());
        event.setUserName(sender.getFirstName() + " " + sender.getLastName());
        messagingTemplate.convertAndSend(resolveTopic(room) + ".typing", event);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageResponse> getHistory(UUID roomId, int page, int size) {
        User requester = accessScopeService.currentUserOrThrow();
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat room not found"));

        assertCanAccessRoom(requester, room);

        return chatMessageRepository
                .findByRoom(room, PageRequest.of(page, size, Sort.by("sentAt").descending()))
                .stream()
                .map(chatMessageMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public List<ChatRoomResponse> getMyRooms() {
        User user = accessScopeService.currentUserOrThrow();
        if (accessScopeService.isAdmin(user)) {
            return List.of();
        }

        UUID officeId = user.getOffice() != null ? user.getOffice().getId() : null;
        UUID departmentId = user.getDepartment() != null ? user.getDepartment().getId() : null;

        if (officeId != null
                && chatRoomRepository.findByTypeAndOffice_Id(ChatRoomType.GENERAL, officeId).isEmpty()) {
            createOfficeGeneralRoom(user.getOffice());
        }

        if (departmentId != null
                && chatRoomRepository.findByTypeAndDepartment_Id(ChatRoomType.DEPARTMENT, departmentId).isEmpty()) {
            createDepartmentRoom(user.getDepartment());
        }

        return chatRoomRepository.findRoomsForUser(officeId, departmentId)
                .stream()
                .map(chatRoomMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public ChatRoom createOfficeGeneralRoom(Office office) {
        if (office == null || office.getId() == null) {
            throw new ResourceNotFoundException("Office not found for chat room creation");
        }

        ChatRoom existing = chatRoomRepository
                .findByTypeAndOffice_Id(ChatRoomType.GENERAL, office.getId())
                .orElse(null);
        if (existing != null) {
            return existing;
        }

        ChatRoom room = new ChatRoom();
        room.setType(ChatRoomType.GENERAL);
        room.setName("General - " + office.getName());
        room.setOffice(office);
        return chatRoomRepository.save(room);
    }

    @Override
    @Transactional
    public ChatRoom createDepartmentRoom(Department department) {
        if (department == null || department.getId() == null) {
            throw new ResourceNotFoundException("Department not found for chat room creation");
        }

        ChatRoom existing = chatRoomRepository
                .findByTypeAndDepartment_Id(ChatRoomType.DEPARTMENT, department.getId())
                .orElse(null);
        if (existing != null) {
            return existing;
        }

        ChatRoom room = new ChatRoom();
        room.setType(ChatRoomType.DEPARTMENT);
        room.setName(department.getName());
        room.setDepartment(department);
        return chatRoomRepository.save(room);
    }

    private void assertCanAccessRoom(User user, ChatRoom room) {
        if (accessScopeService.isAdmin(user)) {
            throw new ForbiddenException("Admin account is not part of chat channels");
        }

        if (room.getType() == ChatRoomType.GENERAL) {
            UUID roomOfficeId = room.getOffice() != null ? room.getOffice().getId() : null;
            UUID userOfficeId = user.getOffice() != null ? user.getOffice().getId() : null;
            if (roomOfficeId == null || !roomOfficeId.equals(userOfficeId)) {
                throw new ForbiddenException("You are not a member of this office general room");
            }
            return;
        }

        if (room.getType() == ChatRoomType.DEPARTMENT) {
            UUID roomDeptId = room.getDepartment() != null ? room.getDepartment().getId() : null;
            UUID userDeptId = user.getDepartment() != null ? user.getDepartment().getId() : null;
            if (roomDeptId == null || !roomDeptId.equals(userDeptId)) {
                throw new ForbiddenException("You are not a member of this department room");
            }
        }
    }

    private String resolveTopic(ChatRoom room) {
        return "/topic/room." + room.getId();
    }
}
