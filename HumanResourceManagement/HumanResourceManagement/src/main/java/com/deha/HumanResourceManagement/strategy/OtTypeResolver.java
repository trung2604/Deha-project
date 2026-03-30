package com.deha.HumanResourceManagement.strategy;

import com.deha.HumanResourceManagement.entity.Office;
import com.deha.HumanResourceManagement.entity.enums.OtType;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

@Component
public class OtTypeResolver {

    private final List<OtTypeStrategy> strategies;

    public OtTypeResolver(List<OtTypeStrategy> strategies) {
        this.strategies = strategies.stream()
                .sorted(Comparator.comparingInt(OtTypeStrategy::priority))
                .toList();
    }

    public OtType resolve(LocalDate date, LocalTime checkOutTime) {
        return resolve(date, checkOutTime, null);
    }

    public OtType resolve(LocalDate date, LocalTime checkOutTime, Office office) {
        LocalTime safeTime = checkOutTime != null ? checkOutTime : LocalTime.NOON;
        return strategies.stream()
                .filter(s -> s.supports(date))
                .findFirst()
                .map(s -> s.resolve(date, safeTime, office))
                .orElse(OtType.WEEKDAY);
    }
}