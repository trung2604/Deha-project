package com.deha.HumanResourceManagement.service;

import com.deha.HumanResourceManagement.entity.AttendanceLog;
import com.deha.HumanResourceManagement.entity.Office;
import com.deha.HumanResourceManagement.entity.User;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface IAttendanceService {
    void checkIn(User user, List<String> clientIps);

    void checkOut(User user, List<String> clientIps);

    int calculateHoursUntil(User user, LocalDate logDate, LocalTime deadlineTime);

    boolean synchronizeDerivedFields(AttendanceLog log);

    int autoCheckout(LocalDate date);

    AttendanceLog getTodayLogOrNull(User user);

    List<AttendanceLog> getDepartmentTodayLogsOrEmpty(User actor);

    void validateOfficeIpAccess(Office office, List<String> clientIps);
}

