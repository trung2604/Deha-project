package com.deha.HumanResourceManagement.strategy;

import java.time.LocalTime;

public final class NightHelper {

    private static final LocalTime NIGHT_START = LocalTime.of(22, 0);
    private static final LocalTime NIGHT_END   = LocalTime.of(6,  0);

    private NightHelper() {}

    public static boolean isNight(LocalTime time) {
        return time.isAfter(NIGHT_START) || time.isBefore(NIGHT_END);
    }
}