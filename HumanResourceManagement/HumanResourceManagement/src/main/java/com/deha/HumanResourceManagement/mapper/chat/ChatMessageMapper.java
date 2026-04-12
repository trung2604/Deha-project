package com.deha.HumanResourceManagement.mapper.chat;

import com.deha.HumanResourceManagement.dto.chat.ChatMessageResponse;
import com.deha.HumanResourceManagement.entity.ChatMessage;
import com.deha.HumanResourceManagement.mapper.HrmMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = HrmMapperConfig.class, uses = ChatMapperSupport.class)
public interface ChatMessageMapper {

    @Mapping(target = "roomId", source = "room.id")
    @Mapping(target = "senderId", source = "sender", qualifiedByName = "senderId")
    @Mapping(target = "senderName", source = "sender", qualifiedByName = "senderName")
    ChatMessageResponse toResponse(ChatMessage message);
}

