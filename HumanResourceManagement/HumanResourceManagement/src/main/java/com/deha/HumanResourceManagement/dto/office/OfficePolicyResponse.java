package com.deha.HumanResourceManagement.dto.office;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfficePolicyResponse {
    private UUID officeId;
    private Long version;
    private String officeName;
    private Integer baseWorkHoursPerDay;
    private Integer otMinHours;
    private LocalTime latestCheckoutTime;
    private LocalTime nightStartTime;
    private LocalTime nightEndTime;
    private Double otWeekdayMultiplier;
    private Double otWeekendMultiplier;
    private Double otHolidayMultiplier;
    private Double otNightBonusMultiplier;
}
