package com.deha.HumanResourceManagement.mapper.ot;

import com.deha.HumanResourceManagement.dto.ot.OtRequestResponse;
import com.deha.HumanResourceManagement.entity.OtRequest;
import com.deha.HumanResourceManagement.mapper.HrmMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = HrmMapperConfig.class, uses = OtMapperSupport.class)
public interface OtRequestMapper {

    @Mapping(target = "userId", source = "user", qualifiedByName = "userId")
    @Mapping(target = "userName", source = "user", qualifiedByName = "userName")
    @Mapping(target = "officeId", source = "office.id")
    @Mapping(target = "approvedById", source = "approvedBy.id")
    @Mapping(target = "approvedByName", source = "approvedBy", qualifiedByName = "userName")
    OtRequestResponse toResponse(OtRequest request);
}

