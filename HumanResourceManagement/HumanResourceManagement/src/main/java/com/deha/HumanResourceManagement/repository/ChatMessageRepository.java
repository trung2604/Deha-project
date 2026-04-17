package com.deha.HumanResourceManagement.repository;

import com.deha.HumanResourceManagement.entity.ChatMessage;
import com.deha.HumanResourceManagement.entity.ChatRoom;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, UUID> {

    List<ChatMessage> findByRoom(ChatRoom room, Pageable pageable);
}
