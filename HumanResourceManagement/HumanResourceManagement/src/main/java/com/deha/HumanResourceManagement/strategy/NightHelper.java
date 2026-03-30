package com.deha.HumanResourceManagement.strategy;

import com.deha.HumanResourceManagement.entity.Office;
import com.deha.HumanResourceManagement.service.support.OfficePolicyService;
import org.springframework.stereotype.Component;

import java.time.LocalTime;

@Component
public class NightHelper {
    private final OfficePolicyService officePolicyService;

    public NightHelper(OfficePolicyService officePolicyService) {
        this.officePolicyService = officePolicyService;
    }

    public boolean isNight(Office office, LocalTime time) {
        if (time == null) {
            return false;
        }

        LocalTime nightStart = officePolicyService.nightStartTime(office);
        LocalTime nightEnd = officePolicyService.nightEndTime(office);
        if (nightStart.equals(nightEnd)) {
            return true;
        }

        if (nightStart.isBefore(nightEnd)) {
            return !time.isBefore(nightStart) && time.isBefore(nightEnd);
        }
        return !time.isBefore(nightStart) || time.isBefore(nightEnd);
    }
}