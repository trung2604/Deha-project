package com.deha.HumanResourceManagement.mapper.chat;

import com.deha.HumanResourceManagement.dto.chat.ChatRoomResponse;
import com.deha.HumanResourceManagement.entity.ChatRoom;
import com.deha.HumanResourceManagement.mapper.HrmMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = HrmMapperConfig.class, uses = ChatMapperSupport.class)
public interface ChatRoomMapper {

    @Mapping(target = "office", source = "office", qualifiedByName = "officeSummary")
    @Mapping(target = "department", source = "department", qualifiedByName = "departmentSummary")
    ChatRoomResponse toResponse(ChatRoom room);
}

