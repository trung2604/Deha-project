package com.deha.HumanResourceManagement.strategy;

import com.deha.HumanResourceManagement.entity.enums.OtType;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;

@Component
public class WeekdayOtStrategy implements OtTypeStrategy {

    @Override
    public boolean supports(LocalDate date) {
        return true;
    }

    @Override
    public OtType resolve(LocalDate date, LocalTime checkOutTime) {
        return NightHelper.isNight(checkOutTime)
                ? OtType.NIGHT_WEEKDAY
                : OtType.WEEKDAY;
    }

    @Override
    public int priority() { return 99; }
}
