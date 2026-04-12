package com.deha.HumanResourceManagement.mapper.ot;

import com.deha.HumanResourceManagement.dto.ot.OtSessionResponse;
import com.deha.HumanResourceManagement.entity.OtSession;
import com.deha.HumanResourceManagement.mapper.HrmMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = HrmMapperConfig.class)
public interface OtSessionMapper {

    @Mapping(target = "otRequestId", source = "session.otRequest.id")
    @Mapping(target = "minimumOtHours", source = "minimumOtHours")
    OtSessionResponse toResponse(OtSession session, int minimumOtHours);
}

