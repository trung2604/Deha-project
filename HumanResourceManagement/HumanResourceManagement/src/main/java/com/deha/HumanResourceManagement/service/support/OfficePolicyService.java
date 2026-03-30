package com.deha.HumanResourceManagement.service.support;

import com.deha.HumanResourceManagement.entity.Office;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Service
public class OfficePolicyService {
    private static final int DEFAULT_STANDARD_WORK_HOURS = 9;
    private static final int DEFAULT_OT_MIN_HOURS = 1;
    private static final double DEFAULT_OT_WEEKDAY_MULTIPLIER = 1.5d;
    private static final double DEFAULT_OT_WEEKEND_MULTIPLIER = 2.0d;
    private static final double DEFAULT_OT_HOLIDAY_MULTIPLIER = 3.0d;
    private static final double DEFAULT_OT_NIGHT_BONUS_MULTIPLIER = 0.3d;
    private static final LocalTime DEFAULT_LATEST_CHECKOUT_TIME = LocalTime.of(22, 0);
    private static final LocalTime DEFAULT_NIGHT_START_TIME = LocalTime.of(22, 0);
    private static final LocalTime DEFAULT_NIGHT_END_TIME = LocalTime.of(6, 0);

    public int standardWorkHours(Office office) {
        return office != null && office.getStandardWorkHours() > 0
                ? office.getStandardWorkHours()
                : DEFAULT_STANDARD_WORK_HOURS;
    }

    public int otMinHours(Office office) {
        return office != null && office.getOtMinHours() > 0
                ? office.getOtMinHours()
                : DEFAULT_OT_MIN_HOURS;
    }

    public LocalTime latestCheckoutTime(Office office) {
        return office != null && office.getLatestCheckoutTime() != null
                ? office.getLatestCheckoutTime()
                : DEFAULT_LATEST_CHECKOUT_TIME;
    }

    public LocalTime nightStartTime(Office office) {
        return office != null && office.getNightStartTime() != null
                ? office.getNightStartTime()
                : DEFAULT_NIGHT_START_TIME;
    }

    public LocalTime nightEndTime(Office office) {
        return office != null && office.getNightEndTime() != null
                ? office.getNightEndTime()
                : DEFAULT_NIGHT_END_TIME;
    }

    public double otWeekdayMultiplier(Office office) {
        return office != null && office.getOtWeekdayMultiplier() > 0
                ? office.getOtWeekdayMultiplier()
                : DEFAULT_OT_WEEKDAY_MULTIPLIER;
    }

    public double otWeekendMultiplier(Office office) {
        return office != null && office.getOtWeekendMultiplier() > 0
                ? office.getOtWeekendMultiplier()
                : DEFAULT_OT_WEEKEND_MULTIPLIER;
    }

    public double otHolidayMultiplier(Office office) {
        return office != null && office.getOtHolidayMultiplier() > 0
                ? office.getOtHolidayMultiplier()
                : DEFAULT_OT_HOLIDAY_MULTIPLIER;
    }

    public double otNightBonusMultiplier(Office office) {
        return office != null && office.getOtNightBonusMultiplier() >= 0
                ? office.getOtNightBonusMultiplier()
                : DEFAULT_OT_NIGHT_BONUS_MULTIPLIER;
    }
}


