package com.deha.HumanResourceManagement.mapper.coreorg;

import com.deha.HumanResourceManagement.entity.Department;
import com.deha.HumanResourceManagement.entity.Office;
import com.deha.HumanResourceManagement.entity.OfficeWifiIp;
import com.deha.HumanResourceManagement.entity.Position;
import org.mapstruct.Named;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class CoreOrgMapperSupport {

    @Named("officeId")
    public UUID officeId(Office office) {
        return office != null ? office.getId() : null;
    }

    @Named("officeName")
    public String officeName(Office office) {
        return office != null ? office.getName() : null;
    }

    @Named("departmentId")
    public UUID departmentId(Department department) {
        return department != null ? department.getId() : null;
    }

    @Named("departmentName")
    public String departmentName(Department department) {
        return department != null ? department.getName() : null;
    }

    @Named("positionId")
    public UUID positionId(Position position) {
        return position != null ? position.getId() : null;
    }

    @Named("positionName")
    public String positionName(Position position) {
        return position != null ? position.getName() : null;
    }

    @Named("officeIdFromDepartment")
    public UUID officeIdFromDepartment(Department department) {
        return department != null && department.getOffice() != null ? department.getOffice().getId() : null;
    }

    @Named("officeNameFromDepartment")
    public String officeNameFromDepartment(Department department) {
        return department != null && department.getOffice() != null ? department.getOffice().getName() : null;
    }

    @Named("wifiIps")
    public List<String> wifiIps(List<OfficeWifiIp> wifiIps) {
        if (wifiIps == null || wifiIps.isEmpty()) {
            return List.of();
        }
        return wifiIps.stream()
                .map(OfficeWifiIp::getIpWifi)
                .toList();
    }
}
