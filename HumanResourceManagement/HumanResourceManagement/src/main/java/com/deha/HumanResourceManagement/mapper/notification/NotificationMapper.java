package com.deha.HumanResourceManagement.mapper.notification;

import com.deha.HumanResourceManagement.dto.notification.NotificationResponse;
import com.deha.HumanResourceManagement.entity.Notification;
import com.deha.HumanResourceManagement.mapper.HrmMapperConfig;
import org.mapstruct.Mapper;

@Mapper(config = HrmMapperConfig.class)
public interface NotificationMapper {
    NotificationResponse toResponse(Notification notification);
}

