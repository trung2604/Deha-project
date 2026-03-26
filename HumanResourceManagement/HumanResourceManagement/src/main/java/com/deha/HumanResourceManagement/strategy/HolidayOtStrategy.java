package com.deha.HumanResourceManagement.strategy;

import com.deha.HumanResourceManagement.entity.enums.OtType;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Component
public class HolidayOtStrategy implements  OtTypeStrategy{

    int year = LocalDate.now().getYear();

    List<LocalDate> holidays = List.of(
            LocalDate.of(year, 1, 1),
            LocalDate.of(year, 4, 30),
            LocalDate.of(year, 5, 1),
            LocalDate.of(year, 9, 2)
    );

    @Override
    public boolean supports(LocalDate date) {
        return holidays.contains(date);
    }

    @Override
    public OtType resolve(LocalDate date, LocalTime checkOutTime) {
        return NightHelper.isNight(checkOutTime)
                ? OtType.NIGHT_HOLIDAY
                : OtType.HOLIDAY;
    }

    @Override
    public int priority() { return 1; }
}
