package com.deha.HumanResourceManagement.mapper.ot;

import com.deha.HumanResourceManagement.dto.ot.OtReportResponse;
import com.deha.HumanResourceManagement.entity.OtReport;
import com.deha.HumanResourceManagement.mapper.HrmMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = HrmMapperConfig.class, uses = OtMapperSupport.class)
public interface OtReportMapper {

    @Mapping(target = "userId", source = "attendanceLog.user", qualifiedByName = "userId")
    @Mapping(target = "userName", source = "attendanceLog.user", qualifiedByName = "userName")
    @Mapping(target = "logDate", source = "attendanceLog.logDate")
    @Mapping(target = "attendanceLogId", source = "attendanceLog.id")
    @Mapping(target = "otRequestId", source = "otRequest.id")
    @Mapping(target = "otSessionId", source = "otSession.id")
    @Mapping(target = "approvedById", source = "approvedBy.id")
    @Mapping(target = "approvedByName", source = "approvedBy", qualifiedByName = "userName")
    OtReportResponse toResponse(OtReport report);
}

