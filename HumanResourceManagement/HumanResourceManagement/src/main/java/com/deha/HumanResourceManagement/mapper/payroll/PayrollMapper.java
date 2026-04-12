package com.deha.HumanResourceManagement.mapper.payroll;

import com.deha.HumanResourceManagement.dto.payroll.PayrollResponse;
import com.deha.HumanResourceManagement.entity.Payroll;
import com.deha.HumanResourceManagement.mapper.HrmMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = HrmMapperConfig.class, uses = PayrollMapperSupport.class)
public interface PayrollMapper {

    @Mapping(target = "year", source = "payYear")
    @Mapping(target = "month", source = "payMonth")
    @Mapping(target = "userId", source = "user", qualifiedByName = "userId")
    @Mapping(target = "userName", source = "user", qualifiedByName = "userName")
    @Mapping(target = "officeId", source = "office", qualifiedByName = "officeId")
    PayrollResponse toResponse(Payroll payroll);
}

