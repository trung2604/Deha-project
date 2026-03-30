package com.deha.HumanResourceManagement.strategy;

import com.deha.HumanResourceManagement.entity.Office;
import com.deha.HumanResourceManagement.entity.enums.OtType;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
public class WeekdayOtStrategy implements OtTypeStrategy {
    private final NightHelper nightHelper;

    public WeekdayOtStrategy(NightHelper nightHelper) {
        this.nightHelper = nightHelper;
    }

    @Override
    public boolean supports(LocalDate date) {
        return true;
    }

    @Override
    public OtType resolve(LocalDate date, LocalTime checkOutTime, Office office) {
        return nightHelper.isNight(office, checkOutTime)
                ? OtType.NIGHT_WEEKDAY
                : OtType.WEEKDAY;
    }

    @Override
    public int priority() { return 99; }
}
