package com.deha.HumanResourceManagement.mapper.attendance;

import com.deha.HumanResourceManagement.dto.attendance.AttendanceLogResponse;
import com.deha.HumanResourceManagement.entity.AttendanceLog;
import com.deha.HumanResourceManagement.mapper.HrmMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = HrmMapperConfig.class, uses = AttendanceMapperSupport.class)
public interface AttendanceMapper {

    @Mapping(target = "userId", source = "user", qualifiedByName = "userId")
    @Mapping(target = "userName", source = "user", qualifiedByName = "userName")
    @Mapping(target = "officeId", source = "office", qualifiedByName = "officeId")
    AttendanceLogResponse toResponse(AttendanceLog log);
}

