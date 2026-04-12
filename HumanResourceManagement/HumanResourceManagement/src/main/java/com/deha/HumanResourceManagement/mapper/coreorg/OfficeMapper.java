package com.deha.HumanResourceManagement.mapper.coreorg;

import com.deha.HumanResourceManagement.dto.office.OfficePolicyResponse;
import com.deha.HumanResourceManagement.dto.office.OfficeResponse;
import com.deha.HumanResourceManagement.entity.Office;
import com.deha.HumanResourceManagement.mapper.HrmMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = HrmMapperConfig.class, uses = CoreOrgMapperSupport.class)
public interface OfficeMapper {

    @Mapping(target = "ipWifiIps", source = "wifiIps", qualifiedByName = "wifiIps")
    @Mapping(target = "baseWorkHoursPerDay", source = "standardWorkHours")
    OfficeResponse toResponse(Office office);

    @Mapping(target = "officeId", source = "id")
    @Mapping(target = "officeName", source = "name")
    @Mapping(target = "baseWorkHoursPerDay", source = "standardWorkHours")
    OfficePolicyResponse toPolicyResponse(Office office);
}
