package com.deha.HumanResourceManagement.mapper.salarycontract;

import com.deha.HumanResourceManagement.dto.salarycontract.SalaryContractResponse;
import com.deha.HumanResourceManagement.entity.SalaryContract;
import com.deha.HumanResourceManagement.mapper.HrmMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = HrmMapperConfig.class, uses = SalaryContractMapperSupport.class)
public interface SalaryContractMapper {

    @Mapping(target = "userId", source = "user", qualifiedByName = "userId")
    @Mapping(target = "userName", source = "user", qualifiedByName = "userName")
    @Mapping(target = "status", source = ".", qualifiedByName = "status")
    SalaryContractResponse toResponse(SalaryContract contract);
}

