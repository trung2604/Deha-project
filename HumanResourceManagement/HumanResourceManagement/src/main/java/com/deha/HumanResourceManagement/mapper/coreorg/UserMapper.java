package com.deha.HumanResourceManagement.mapper.coreorg;

import com.deha.HumanResourceManagement.dto.user.UserResponse;
import com.deha.HumanResourceManagement.entity.User;
import com.deha.HumanResourceManagement.mapper.HrmMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(config = HrmMapperConfig.class, uses = CoreOrgMapperSupport.class)
public interface UserMapper {

    @Mapping(target = "officeId", source = "office", qualifiedByName = "officeId")
    @Mapping(target = "officeName", source = "office", qualifiedByName = "officeName")
    @Mapping(target = "departmentId", source = "department", qualifiedByName = "departmentId")
    @Mapping(target = "departmentName", source = "department", qualifiedByName = "departmentName")
    @Mapping(target = "positionId", source = "position", qualifiedByName = "positionId")
    @Mapping(target = "positionName", source = "position", qualifiedByName = "positionName")
    UserResponse toResponse(User user);
}
