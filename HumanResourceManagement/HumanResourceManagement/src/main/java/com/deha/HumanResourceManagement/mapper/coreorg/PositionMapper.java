package com.deha.HumanResourceManagement.mapper.coreorg;

import com.deha.HumanResourceManagement.dto.position.PositionResponse;
import com.deha.HumanResourceManagement.entity.Position;
import com.deha.HumanResourceManagement.mapper.HrmMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = HrmMapperConfig.class, uses = CoreOrgMapperSupport.class)
public interface PositionMapper {

    @Mapping(target = "departmentId", source = "department", qualifiedByName = "departmentId")
    @Mapping(target = "departmentName", source = "department", qualifiedByName = "departmentName")
    @Mapping(target = "officeId", source = "department", qualifiedByName = "officeIdFromDepartment")
    @Mapping(target = "officeName", source = "department", qualifiedByName = "officeNameFromDepartment")
    PositionResponse toResponse(Position position);
}
