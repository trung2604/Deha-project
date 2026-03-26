package com.deha.HumanResourceManagement.strategy;

import com.deha.HumanResourceManagement.entity.enums.OtType;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Set;

@Component
public class WeekendOtStrategy implements OtTypeStrategy {

    private static final Set<DayOfWeek> WEEKEND = EnumSet.of(
            DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
    );
    @Override
    public boolean supports(LocalDate date) {
        return date != null && WEEKEND.contains(date.getDayOfWeek());
    }

    @Override
    public OtType resolve(LocalDate date, LocalTime checkOutTime) {
        return NightHelper.isNight(checkOutTime)
                ? OtType.NIGHT_WEEKEND
                : OtType.WEEKEND;
    }

    @Override
    public int priority() { return 2; }
}
