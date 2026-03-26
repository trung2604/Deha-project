package com.deha.HumanResourceManagement.strategy;

import com.deha.HumanResourceManagement.entity.enums.OtType;

import java.time.LocalDate;
import java.time.LocalTime;

public interface OtTypeStrategy {
    boolean supports(LocalDate date);
    OtType resolve (LocalDate date, LocalTime checkOutTime);
    int     priority();
}