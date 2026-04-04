package com.deha.HumanResourceManagement.config.security;

import com.deha.HumanResourceManagement.entity.enums.Permission;
import com.deha.HumanResourceManagement.entity.enums.Role;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;

@Component
public class RolePermissionResolver {

    public Set<Permission> resolve(Role role) {
        if (role == null) {
            return Set.of();
        }
        return switch (role) {
            case ADMIN -> adminPermissions();
            case MANAGER_OFFICE -> officeManagerPermissions();
            case MANAGER_DEPARTMENT -> departmentManagerPermissions();
            case EMPLOYEE -> Set.of();
        };
    }

    private Set<Permission> adminPermissions() {
        return EnumSet.of(
                Permission.USER_VIEW,
                Permission.USER_MANAGE,
                Permission.DEPARTMENT_VIEW,
                Permission.DEPARTMENT_MANAGE,
                Permission.POSITION_VIEW,
                Permission.POSITION_MANAGE,
                Permission.OFFICE_VIEW,
                Permission.OFFICE_MANAGE,
                Permission.SALARY_CONTRACT_VIEW,
                Permission.SALARY_CONTRACT_MANAGE,
                Permission.PAYROLL_VIEW,
                Permission.PAYROLL_GENERATE
        );
    }

    private Set<Permission> officeManagerPermissions() {
        return EnumSet.of(
                Permission.USER_VIEW,
                Permission.USER_MANAGE,
                Permission.DEPARTMENT_VIEW,
                Permission.DEPARTMENT_MANAGE,
                Permission.POSITION_VIEW,
                Permission.POSITION_MANAGE,
                Permission.OFFICE_VIEW,
                Permission.OFFICE_POLICY_VIEW,
                Permission.OFFICE_POLICY_UPDATE,
                Permission.SALARY_CONTRACT_VIEW,
                Permission.SALARY_CONTRACT_MANAGE,
                Permission.PAYROLL_VIEW,
                Permission.PAYROLL_GENERATE,
                Permission.OT_REQUEST_APPROVAL_VIEW,
                Permission.OT_REQUEST_APPROVE,
                Permission.OT_REPORT_APPROVAL_VIEW,
                Permission.OT_REPORT_APPROVE
        );
    }

    private Set<Permission> departmentManagerPermissions() {
        return EnumSet.of(
                Permission.USER_VIEW,
                Permission.DEPARTMENT_VIEW,
                Permission.POSITION_VIEW,
                Permission.OT_REQUEST_APPROVAL_VIEW,
                Permission.OT_REQUEST_APPROVE,
                Permission.OT_REPORT_APPROVAL_VIEW,
                Permission.OT_REPORT_APPROVE
        );
    }
}

