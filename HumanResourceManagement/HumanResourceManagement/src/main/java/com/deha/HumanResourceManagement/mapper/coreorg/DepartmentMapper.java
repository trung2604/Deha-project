package com.deha.HumanResourceManagement.mapper.coreorg;

import com.deha.HumanResourceManagement.dto.department.DepartmentDetailResponse;
import com.deha.HumanResourceManagement.dto.department.DepartmentPositionItem;
import com.deha.HumanResourceManagement.dto.department.DepartmentResponse;
import com.deha.HumanResourceManagement.dto.department.DepartmentUserItem;
import com.deha.HumanResourceManagement.entity.Department;
import com.deha.HumanResourceManagement.entity.Position;
import com.deha.HumanResourceManagement.entity.User;
import com.deha.HumanResourceManagement.mapper.HrmMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = HrmMapperConfig.class, uses = CoreOrgMapperSupport.class)
public interface DepartmentMapper {

    @Mapping(target = "officeId", source = "office", qualifiedByName = "officeId")
    @Mapping(target = "officeName", source = "office", qualifiedByName = "officeName")
    DepartmentResponse toResponse(Department department);

    @Mapping(target = "officeId", source = "office", qualifiedByName = "officeId")
    @Mapping(target = "officeName", source = "office", qualifiedByName = "officeName")
    DepartmentDetailResponse toDetailResponse(Department department);

    @Mapping(target = "positionId", source = "position", qualifiedByName = "positionId")
    @Mapping(target = "positionName", source = "position", qualifiedByName = "positionName")
    DepartmentUserItem toUserItem(User user);

    DepartmentPositionItem toPositionItem(Position position);
}

