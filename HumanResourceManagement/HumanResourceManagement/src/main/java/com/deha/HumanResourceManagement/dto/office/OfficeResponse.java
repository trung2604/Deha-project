package com.deha.HumanResourceManagement.dto.office;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OfficeResponse {
    private UUID id;
    private Long version;
    private String name;
    private String description;
    private List<String> ipWifiIps;
    private Integer baseWorkHoursPerDay;
    private Double otWeekdayMultiplier;
    private Double otWeekendMultiplier;
    private Double otHolidayMultiplier;
    private Double otNightBonusMultiplier;
    private Integer otMinHours;
    private LocalTime latestCheckoutTime;
}
